package komu.blunt.types.checker

import komu.blunt.eval.TypeCheckException
import komu.blunt.types.Predicate
import komu.blunt.types.Type
import komu.blunt.types.patterns.Pattern
import komu.blunt.types.typeFromObject
import java.util.*

class PatternTypeChecker(private val tc: TypeChecker) {

    fun typeCheck(pattern: Pattern): PatternTypeCheckResult<Type> =
        when (pattern) {
            is Pattern.Variable    -> typeCheckVariable(pattern)
            is Pattern.Wildcard    -> PatternTypeCheckResult(tc.newTVar())
            is Pattern.Literal     -> PatternTypeCheckResult(typeFromObject(pattern.value))
            is Pattern.Constructor -> typeCheckConstructor(pattern)
        }

    private fun typeCheckVariable(pattern: Pattern.Variable): PatternTypeCheckResult<Type> {
        val tv = tc.newTVar()
        return PatternTypeCheckResult(tv, Assumptions.singleton(pattern.variable, tv.toScheme()))
    }

    private fun typeCheckConstructor(pattern: Pattern.Constructor): PatternTypeCheckResult<Type> {
        val constructor = tc.findConstructor(pattern.name)

        if (pattern.args.size != constructor.arity)
            throw TypeCheckException("invalid amount of arguments for constructor '${pattern.name}'; expected ${constructor.arity}, but got ${pattern.args.size}")

        val result = assumptionsFrom(pattern.args)
        val t = tc.newTVar()

        val q = tc.freshInstance(constructor.scheme)

        tc.unify(q.value, Type.function(result.value, t))

        return PatternTypeCheckResult(t, result.ass, result.predicates + q.predicates)
    }

    private fun assumptionsFrom(patterns: List<Pattern>): PatternTypeCheckResult<List<Type>> {
        val predicates = ArrayList<Predicate>()
        var ass = Assumptions.empty

        val types = patterns.map { pattern ->
            val result = tc.typeCheck(pattern)

            predicates += result.predicates
            ass += result.ass
            result.value
        }

        return PatternTypeCheckResult(types, ass, predicates)
    }
}
