package komu.blunt.types.checker

import komu.blunt.eval.TypeCheckException
import komu.blunt.types.*
import komu.blunt.types.patterns.*

import java.util.ArrayList
import java.util.List

import komu.blunt.types.functionType

class PatternTypeChecker(private val tc: TypeChecker) {

    fun typeCheck(pattern: Pattern): PatternTypeCheckResult<Type> =
        when (pattern) {
            is VariablePattern    -> typeCheckVariable(pattern)
            is WildcardPattern    -> PatternTypeCheckResult(Assumptions.empty(), tc.newTVar())
            is LiteralPattern     -> PatternTypeCheckResult(Assumptions.empty(), typeFromObject(pattern.value))
            is ConstructorPattern -> typeCheckConstructor(pattern)
            else                  -> throw Exception("invalid pattern $pattern")
        }

    private fun typeCheckVariable(pattern: VariablePattern): PatternTypeCheckResult<Type> {
        val tv = tc.newTVar()
        return PatternTypeCheckResult(Assumptions.singleton(pattern.variable, Scheme.fromType(tv)), tv)
    }

    private fun typeCheckConstructor(pattern: ConstructorPattern): PatternTypeCheckResult<Type> {
        val constructor = tc.findConstructor(pattern.name)

        if (pattern.args.size() != constructor.arity)
            throw TypeCheckException("invalid amount of arguments for constructor '${pattern.name}'; expected ${constructor.arity}, but got ${pattern.args.size()}")

        val result = assumptionsFrom(pattern.args)
        val t = tc.newTVar()

        val q = tc.freshInstance(constructor.scheme)

        tc.unify(q.value.sure(), functionType(result.value, t).sure())

        val predicates = ArrayList<Predicate>()

        predicates.addAll(result.predicates)
        predicates.addAll(q.predicates)

        return PatternTypeCheckResult(predicates, result.ass, t)
    }

    private fun assumptionsFrom(patterns: List<Pattern>): PatternTypeCheckResult<List<Type?>> {
        val predicates = ArrayList<Predicate>()
        var ass = Assumptions.empty()
        val types = ArrayList<Type?>(patterns.size())

        for (val pattern in patterns) {
            val result = tc.typeCheck(pattern)

            predicates.addAll(result.predicates)
            ass = ass.join(result.ass)
            types.add(result.value)
        }
        return PatternTypeCheckResult(predicates, ass, types)
    }
}
