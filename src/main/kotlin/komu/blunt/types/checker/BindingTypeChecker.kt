package komu.blunt.types.checker

import komu.blunt.ast.ASTExpression
import komu.blunt.ast.BindGroup
import komu.blunt.ast.ExplicitBinding
import komu.blunt.ast.ImplicitBinding
import komu.blunt.types.*
import komu.blunt.utils.Pair

import java.util.ArrayList
import java.util.HashSet
import java.util.List
import java.util.Set

import komu.blunt.ast.ImplicitBinding.bindingNames
import komu.blunt.types.Qualified.quantify
import komu.blunt.types.Type.toSchemes
import komu.blunt.utils.CollectionUtils.intersection
import java.util.LinkedHashSet

final class BindingTypeChecker(private val tc: TypeChecker) {


    fun typeCheckBindGroup(bindings: BindGroup, ass: Assumptions): TypeCheckResult<Assumptions> {
        val result = TypeCheckResult.builder<Assumptions>().sure()

        val explicitAssumptions = bindings.assumptionFromExplicitBindings()

        val res = typeCheckImplicits(bindings, ass.join(explicitAssumptions).sure())
        val newAssumptions = res.value.join(explicitAssumptions).sure()
        result.addPredicates(res.predicates)
        result.addPredicates(typeCheckExplicits(bindings, ass.join(newAssumptions).sure()))

        return result.build(newAssumptions).sure()
    }

    private fun typeCheckImplicits(bindings: BindGroup, ass: Assumptions): TypeCheckResult<Assumptions>  {
        val result = TypeCheckResult.builder<Assumptions>().sure()
        val assumptions = Assumptions.builder().sure()

        for (val bs in bindings.implicitBindings) {
            val res = typeCheckImplicitGroup(bs.sure(), assumptions.build(ass).sure())
            result.addPredicates(res.predicates)
            assumptions.addAll(res.value)
        }

        return result.build(assumptions.build().sure()).sure()
    }

    private fun typeCheckExplicits(bindGroup: BindGroup, ass: Assumptions): List<Predicate?> {
        val predicates = ArrayList<Predicate?>()

        for (val b in bindGroup.explicitBindings)
            predicates.addAll(typeCheck(b.sure(), ass))

        return predicates
    }

    private fun typeCheck(binding: ExplicitBinding, ass: Assumptions): List<Predicate?> {
        throw UnsupportedOperationException("explicit bindings are not implemented")
    }

    private fun typeCheckImplicitGroup(bindings: List<ImplicitBinding?>, ass: Assumptions): TypeCheckResult<Assumptions> {
        val typeVariables = tc.newTVars(bindings.size())
        val predicates = typeCheckAndUnifyBindings(bindings, typeVariables, ass)

        val types = tc.applySubstitution(typeVariables)
        val fs = getTypeVariables(tc.applySubstitution(ass))

        val vss = ArrayList<Set<TypeVariable?>?>(types.size())

        val genericVariables = HashSet<TypeVariable?>();
        for (val t in types) {
            val vars = getTypeVariables(t.sure())
            vss.add(vars)
            genericVariables.addAll(vars)
        }

        genericVariables.removeAll(fs)
        val sharedVariables = intersection(vss).sure()

        val split = tc.classEnv.split(fs, sharedVariables, predicates)
        val deferredPredicates = split.first
        val retainedPredicates = split.second

        val finalSchemes = ArrayList<Scheme?>(types.size())
        for (val t in types)
            finalSchemes.add(quantify(genericVariables, Qualified(retainedPredicates, t)))

        val finalAssumptions = Assumptions.from(bindingNames(bindings), finalSchemes).sure()
        return TypeCheckResult.of(finalAssumptions, deferredPredicates.sure())
    }

    private fun typeCheckAndUnifyBindings(bs: List<ImplicitBinding?>, ts: List<Type?>, ass: Assumptions): List<Predicate?> {
        val as2 = Assumptions.from(bindingNames(bs), toSchemes(ts))?.join(ass).sure()

        val predicates = ArrayList<Predicate?>();

        for (val i in 0..ts.size()-1) {
            val exp = bs.get(i)?.expr.sure()
            val typ = ts.get(i).sure()

            val res = tc.typeCheck(exp, as2)
            tc.unify(res.value.sure(), typ)
            predicates.addAll(res.predicates.sure())
        }

        return tc.applySubstitution(predicates)
    }

    // TODO: duplication
    private fun getTypeVariables(t: Type): Set<TypeVariable?> {
        val types = LinkedHashSet<TypeVariable?>()
        t.addTypeVariables(types)
        return types
    }

    private fun getTypeVariables(t: Assumptions): Set<TypeVariable?> {
        val types = LinkedHashSet<TypeVariable?>()
        t.addTypeVariables(types)
        return types
    }
}
