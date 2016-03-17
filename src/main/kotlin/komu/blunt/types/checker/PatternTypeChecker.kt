package komu.blunt.types.checker

import java.util.ArrayList
import java.util.Collections.emptyList
import komu.blunt.eval.TypeCheckException
import komu.blunt.types.*
import komu.blunt.types.patterns.*
import komu.blunt.utils.concat

class PatternTypeChecker(private val tc: TypeChecker) {

    fun typeCheck(pattern: Pattern): PatternTypeCheckResult<Type> =
        when (pattern) {
            is Pattern.Variable -> typeCheckVariable(pattern)
            is Pattern.Wildcard -> PatternTypeCheckResult(emptyList(), Assumptions.empty(), tc.newTVar())
            is Pattern.Literal -> PatternTypeCheckResult(emptyList(), Assumptions.empty(), typeFromObject(pattern.value))
            is Pattern.Constructor -> typeCheckConstructor(pattern)
            else                  -> throw Exception("invalid pattern $pattern")
        }

    private fun typeCheckVariable(pattern: Pattern.Variable): PatternTypeCheckResult<Type> {
        val tv = tc.newTVar()
        return PatternTypeCheckResult(emptyList(), Assumptions.singleton(pattern.variable, tv.toScheme()), tv)
    }

    private fun typeCheckConstructor(pattern: Pattern.Constructor): PatternTypeCheckResult<Type> {
        val constructor = tc.findConstructor(pattern.name)

        if (pattern.args.size != constructor.arity)
            throw TypeCheckException("invalid amount of arguments for constructor '${pattern.name}'; expected ${constructor.arity}, but got ${pattern.args.size}")

        val result = assumptionsFrom(pattern.args)
        val t = tc.newTVar()

        val q = tc.freshInstance(constructor.scheme)

        tc.unify(q.value, functionType(result.value, t))

        val predicates = result.predicates.concat(q.predicates)

        return PatternTypeCheckResult(predicates, result.ass, t)
    }

    private fun assumptionsFrom(patterns: List<Pattern>): PatternTypeCheckResult<List<Type>> {
        val predicates = ArrayList<Predicate>()
        var ass = Assumptions.empty()
        val types = ArrayList<Type>()

        for (pattern in patterns) {
            val result = tc.typeCheck(pattern)

            predicates.addAll(result.predicates)
            ass = ass.join(result.ass)
            types.add(result.value)
        }

        return PatternTypeCheckResult(predicates, ass, types)
    }
}
