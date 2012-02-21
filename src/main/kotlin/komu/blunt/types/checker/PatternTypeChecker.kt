package komu.blunt.types.checker

import komu.blunt.eval.TypeCheckException
import komu.blunt.types.*
import komu.blunt.types.patterns.*

import java.util.ArrayList
import java.util.List

import komu.blunt.types.Type.functionType

class PatternTypeChecker(private val tc: TypeChecker) {

    fun typeCheck(pattern: Pattern): PatternTypeCheckResult<Type> =
        when (pattern) {
            is VariablePattern    -> typeCheckVariable(pattern)
            is WildcardPattern    -> PatternTypeCheckResult(Assumptions.empty().sure(), tc.newTVar())
            is LiteralPattern     -> PatternTypeCheckResult(Assumptions.empty().sure(), Type.fromObject(pattern.value).sure())
            is ConstructorPattern -> typeCheckConstructor(pattern)
            else                  -> throw Exception("invalid pattern $pattern")
        }

    private fun typeCheckVariable(pattern: VariablePattern): PatternTypeCheckResult<Type> {
        val tv = tc.newTVar()
        return PatternTypeCheckResult(Assumptions.singleton(pattern.variable, tv.toScheme()).sure(), tv)
    }

    private fun typeCheckConstructor(pattern: ConstructorPattern): PatternTypeCheckResult<Type> {
        val constructor = tc.findConstructor(pattern.name.sure())

        if (pattern.args?.size() != constructor.arity)
            throw TypeCheckException("invalid amount of arguments for constructor '${pattern.name}'; expected ${constructor.arity}, but got ${pattern.args?.size()}")

        val result = assumptionsFrom(pattern.args.sure())
        val t = tc.newTVar()

        val q = tc.freshInstance(constructor.scheme.sure())

        tc.unify(q.value.sure(), functionType(result.value, t).sure())

        val predicates = ArrayList<Predicate?>()
        for (val p in result.predicates)
            predicates.add(p)
        for (val p in q.predicates)
            predicates.add(p)

        return PatternTypeCheckResult(predicates, result.ass, t)
    }

    private fun assumptionsFrom(patterns: List<Pattern?>): PatternTypeCheckResult<List<Type?>> {
        val predicates = ArrayList<Predicate?>()
        var ass = Assumptions.empty().sure()
        val types = ArrayList<Type?>(patterns.size())

        for (val pattern in patterns) {
            val result = tc.typeCheck(pattern.sure())

            for (val p in result.predicates)
                predicates.add(p)
            ass = ass.join(result.ass).sure()
            types.add(result.value)
        }
        return PatternTypeCheckResult(predicates, ass, types)
    }
}

