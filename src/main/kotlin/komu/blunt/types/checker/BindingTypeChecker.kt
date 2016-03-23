package komu.blunt.types.checker

import komu.blunt.ast.*
import komu.blunt.types.Predicate
import komu.blunt.types.Qualified
import komu.blunt.types.Type
import komu.blunt.types.quantify
import komu.blunt.utils.intersection
import java.util.*

final class BindingTypeChecker(private val tc: TypeChecker) {

    fun typeCheckBindGroup(bindings: BindGroup, ass: Assumptions): TypeCheckResult<Assumptions> {
        val explicitAssumptions = bindings.assumptionFromExplicitBindings()

        val (implicitAssumptions, predicates) = typeCheckImplicits(bindings, ass + explicitAssumptions)

        return TypeCheckResult(implicitAssumptions + explicitAssumptions,
                predicates + typeCheckExplicits(bindings, ass + (implicitAssumptions + explicitAssumptions)))
    }

    private fun typeCheckImplicits(bindings: BindGroup, ass: Assumptions): TypeCheckResult<Assumptions> {
        val resultPredicates = ArrayList<Predicate>()
        val assumptions = Assumptions.builder()

        for (bindingGroup in bindings.implicitBindings) {
            val (newAssumptions, predicates) = typeCheckImplicitGroup(bindingGroup, assumptions.build(ass))
            resultPredicates += predicates
            assumptions += newAssumptions
        }

        return TypeCheckResult(assumptions.build(), resultPredicates)
    }

    private fun typeCheckExplicits(bindGroup: BindGroup, ass: Assumptions): List<Predicate> =
        bindGroup.explicitBindings.flatMap { typeCheck(it, ass) }

    private fun typeCheck(binding: ExplicitBinding, ass: Assumptions): List<Predicate> {
        throw UnsupportedOperationException("explicit bindings are not implemented")
    }

    private fun typeCheckImplicitGroup(bindings: List<ImplicitBinding>, ass: Assumptions): TypeCheckResult<Assumptions> {
        val typeVariables = tc.newTVars(bindings.size)

        val bindingsAndVars = bindings.zip(typeVariables)
        val predicates = typeCheckAndUnifyBindings(bindingsAndVars, ass)

        val types = tc.applySubstitution(typeVariables)
        val fs = tc.applySubstitution(ass).typeVarsSet()

        val vss = ArrayList<Set<Type.Var>>()

        val genericVariables = HashSet<Type.Var>()
        for (t in types) {
            val vars = t.typeVarsSet()
            vss.add(vars)
            genericVariables.addAll(vars)
        }

        genericVariables.removeAll(fs)

        val (deferredPredicates, retainedPredicates) = tc.classEnv.split(fs, intersection(vss), predicates)

        val finalSchemes = types.map { Qualified(retainedPredicates, it).quantify(genericVariables) }

        val finalAssumptions = Assumptions.from(bindings.names().zip(finalSchemes))
        return TypeCheckResult(finalAssumptions, deferredPredicates)
    }

    private fun typeCheckAndUnifyBindings(bindingsWithTypes: List<Pair<ImplicitBinding, Type>>, ass: Assumptions): List<Predicate> {
        val augmentedAssumptions = Assumptions.from(bindingsWithTypes.instantiate()) + ass

        return tc.applySubstitution(bindingsWithTypes.flatMap {
            val (binding, type) = it

            val (t, ps) = tc.typeCheck(binding.expr, augmentedAssumptions)
            tc.unify(t, type)
            ps
        })
    }
}
