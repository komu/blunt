package komu.blunt.types.checker

import java.util.LinkedHashSet
import komu.blunt.ast.BindGroup
import komu.blunt.ast.ExplicitBinding
import komu.blunt.ast.ImplicitBinding
import komu.blunt.ast.names
import komu.blunt.types.*
import komu.blunt.utils.intersection

final class BindingTypeChecker(private val tc: TypeChecker) {

    fun typeCheckBindGroup(bindings: BindGroup, ass: Assumptions): TypeCheckResult<Assumptions> {
        val result = TypeCheckResult.builder<Assumptions>()

        val explicitAssumptions = bindings.assumptionFromExplicitBindings()

        val res = typeCheckImplicits(bindings, ass.join(explicitAssumptions))
        val newAssumptions = res.value.join(explicitAssumptions)
        result.addPredicates(res.predicates)
        result.addPredicates(typeCheckExplicits(bindings, ass.join(newAssumptions)))

        return result.build(newAssumptions)
    }

    private fun typeCheckImplicits(bindings: BindGroup, ass: Assumptions): TypeCheckResult<Assumptions>  {
        val result = TypeCheckResult.builder<Assumptions>()
        val assumptions = Assumptions.builder()

        for (val bs in bindings.implicitBindings) {
            val res = typeCheckImplicitGroup(bs, assumptions.build(ass))
            result.addPredicates(res.predicates)
            assumptions.addAll(res.value)
        }

        return result.build(assumptions.build())
    }

    private fun typeCheckExplicits(bindGroup: BindGroup, ass: Assumptions): List<Predicate> {
        val predicates = arrayList<Predicate>()

        for (val b in bindGroup.explicitBindings)
            predicates.addAll(typeCheck(b, ass))

        return predicates
    }

    private fun typeCheck(binding: ExplicitBinding, ass: Assumptions): List<Predicate> {
        throw UnsupportedOperationException("explicit bindings are not implemented")
    }

    private fun typeCheckImplicitGroup(bindings: List<ImplicitBinding>, ass: Assumptions): TypeCheckResult<Assumptions> {
        val typeVariables = tc.newTVars(bindings.size)
        val predicates = typeCheckAndUnifyBindings(bindings, typeVariables, ass)

        val types = tc.applySubstitution(typeVariables)
        val fs = getTypeVariables(tc.applySubstitution(ass))

        val vss = arrayList<Set<TypeVariable>>()

        val genericVariables = hashSet<TypeVariable>()
        for (val t in types) {
            val vars = getTypeVariables(t)
            vss.add(vars)
            genericVariables.addAll(vars)
        }

        genericVariables.removeAll(fs)

        val (deferredPredicates, retainedPredicates) = tc.classEnv.split(fs, intersection(vss), predicates)

        val finalSchemes = types.map { quantify(genericVariables, Qualified(retainedPredicates, it)) }

        val finalAssumptions = Assumptions.from(bindings.names(), finalSchemes)
        return TypeCheckResult.of(finalAssumptions, deferredPredicates)
    }

    private fun typeCheckAndUnifyBindings(bs: List<ImplicitBinding>, ts: List<Type>, ass: Assumptions): List<Predicate> {
        val as2 = Assumptions.from(bs.names(), Scheme.fromTypes(ts)).join(ass)

        val predicates = arrayList<Predicate>()

        for (val i in ts.indices) {
            val exp = bs[i].expr
            val typ = ts[i]

            val res = tc.typeCheck(exp, as2)
            tc.unify(res.value, typ)
            predicates.addAll(res.predicates)
        }

        return tc.applySubstitution(predicates)
    }

    // TODO: duplication
    private fun getTypeVariables(t: Type): Set<TypeVariable> {
        val types = LinkedHashSet<TypeVariable>()
        t.addTypeVariables(types)
        return types
    }

    private fun getTypeVariables(t: Assumptions): Set<TypeVariable> {
        val types = LinkedHashSet<TypeVariable>()
        t.addTypeVariables(types)
        return types
    }
}
