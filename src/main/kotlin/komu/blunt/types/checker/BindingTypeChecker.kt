package komu.blunt.types.checker

import komu.blunt.ast.BindGroup
import komu.blunt.ast.ExplicitBinding
import komu.blunt.ast.ImplicitBinding
import komu.blunt.ast.names
import komu.blunt.types.*
import komu.blunt.utils.intersection

final class BindingTypeChecker(private val tc: TypeChecker) {

    fun typeCheckBindGroup(bindings: BindGroup, ass: Assumptions): TypeCheckResult<Assumptions> {
        val explicitAssumptions = bindings.assumptionFromExplicitBindings()

        val (implicitAssumptions, predicates) = typeCheckImplicits(bindings, ass.join(explicitAssumptions))
        val joinedAssumptions = implicitAssumptions.join(explicitAssumptions)

        val result = TypeCheckResult.builder<Assumptions>()
        result.addPredicates(predicates)
        result.addPredicates(typeCheckExplicits(bindings, ass.join(joinedAssumptions)))
        return result.build(joinedAssumptions)
    }

    private fun typeCheckImplicits(bindings: BindGroup, ass: Assumptions): TypeCheckResult<Assumptions> {
        val result = TypeCheckResult.builder<Assumptions>()
        val assumptions = Assumptions.builder()

        for (bindingGroup in bindings.implicitBindings) {
            val (newAssumptions, predicates) = typeCheckImplicitGroup(bindingGroup, assumptions.build(ass))
            result.addPredicates(predicates)
            assumptions.addAll(newAssumptions)
        }

        return result.build(assumptions.build())
    }

    private fun typeCheckExplicits(bindGroup: BindGroup, ass: Assumptions): List<Predicate> {
        val predicates = arrayList<Predicate>()

        for (binding in bindGroup.explicitBindings)
            predicates.addAll(typeCheck(binding, ass))

        return predicates
    }

    private fun typeCheck(binding: ExplicitBinding, ass: Assumptions): List<Predicate> {
        throw UnsupportedOperationException("explicit bindings are not implemented")
    }

    private fun typeCheckImplicitGroup(bindings: List<ImplicitBinding>, ass: Assumptions): TypeCheckResult<Assumptions> {
        val typeVariables = tc.newTVars(bindings.size)
        val predicates = typeCheckAndUnifyBindings(bindings, typeVariables, ass)

        val types = tc.applySubstitution(typeVariables)
        val fs = tc.applySubstitution(ass).typeVariables

        val vss = arrayList<Set<TypeVariable>>()

        val genericVariables = hashSet<TypeVariable>()
        for (val t in types) {
            val vars = t.typeVariables
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

        for ((i, typ) in ts.withIndices()) {
            val (t, ps) = tc.typeCheck(bs[i].expr, as2)

            tc.unify(t, typ)
            predicates.addAll(ps)
        }

        return tc.applySubstitution(predicates)
    }
}
