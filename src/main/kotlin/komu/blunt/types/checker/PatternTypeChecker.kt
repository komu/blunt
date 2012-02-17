package komu.blunt.types.checker

import komu.blunt.eval.TypeCheckException
import komu.blunt.types.*
import komu.blunt.types.patterns.*

import java.util.ArrayList
import java.util.List

import komu.blunt.types.Type.functionType

class PatternTypeChecker(private val tc: TypeChecker) : PatternVisitor<Unit,PatternTypeCheckResult<Type?>> {

    override fun visit(pattern: VariablePattern?, ctx: Unit): PatternTypeCheckResult<Type?> {
        val tv = tc.newTVar()
        return PatternTypeCheckResult(Assumptions.singleton(pattern?.`var`, tv.toScheme()), tv)
    }

    override fun visit(pattern: WildcardPattern?, ctx: Unit): PatternTypeCheckResult<Type?> =
        PatternTypeCheckResult(Assumptions.empty(), tc.newTVar())

    override fun visit(pattern: LiteralPattern?, ctx: Unit): PatternTypeCheckResult<Type?> =
        PatternTypeCheckResult(Assumptions.empty(), Type.fromObject(pattern?.value))

    override fun visit(pattern: ConstructorPattern?, ctx: Tuple0): PatternTypeCheckResult<Type?> {
        val constructor = tc.findConstructor(pattern?.name.sure())

        if (pattern?.args?.size() != constructor.arity)
            throw TypeCheckException("invalid amount of arguments for constructor '${pattern?.name}'; expected ${constructor.arity}, but got ${pattern?.args?.size()}")

        val result = assumptionsFrom(pattern?.args.sure())
        val t = tc.newTVar()

        val q = tc.freshInstance(constructor.scheme.sure())

        tc.unify(q.value.sure(), functionType(result.value, t).sure())

        val predicates = ArrayList<Predicate?>()
        for (val p in result.predicates)
            predicates.add(p)
        for (val p in q.predicates)
            predicates.add(p)

        return PatternTypeCheckResult(predicates, result.`as`, t)
    }

    private fun assumptionsFrom(patterns: List<Pattern?>): PatternTypeCheckResult<List<Type?>> {
        val predicates = ArrayList<Predicate?>()
        var ass = Assumptions.empty().sure()
        val types = ArrayList<Type?>(patterns.size())

        for (val pattern in patterns) {
            val result = tc.typeCheck(pattern.sure())

            for (val p in result.predicates)
                predicates.add(p)
            ass = ass.join(result.`as`).sure()
            types.add(result.value)
        }
        return PatternTypeCheckResult(predicates, ass, types)
    }
}

