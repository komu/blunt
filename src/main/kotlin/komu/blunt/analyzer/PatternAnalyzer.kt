package komu.blunt.analyzer

import komu.blunt.core.*
import komu.blunt.types.patterns.*

import java.util.ArrayList
import java.util.List

class PatternAnalyzer {

    fun createExtractor(pattern: Pattern, matchedObject: VariableReference, env: StaticEnvironment ): PatternExtractor {
        val predicate = makePredicate(pattern, PatternPath.EMPTY.sure(), matchedObject)
        val extractor = makeExtractor(pattern, PatternPath.EMPTY.sure(), env, matchedObject)
        return PatternExtractor(predicate, extractor)
    }

    private fun makeExtractor(pattern: Pattern, path: PatternPath, env: StaticEnvironment, matchedObject: VariableReference): CoreExpression =
        when (pattern) {
            is WildcardPattern    -> CoreEmptyExpression.INSTANCE.sure()
            is LiteralPattern     -> CoreEmptyExpression.INSTANCE.sure()
            is VariablePattern    -> variableExtractor(pattern, path, env, matchedObject)
            is ConstructorPattern -> constructorExtractor(pattern, path, env, matchedObject)
            else -> throw Exception()
        }

    private fun variableExtractor(pattern: VariablePattern, path: PatternPath, env: StaticEnvironment, matchedObject: VariableReference): CoreExpression {
        val v = env.lookupInCurrentScopeOrDefine(pattern.variable.sure())
        return CoreSetExpression(v, CoreExtractExpression(matchedObject, path))
    }

    private fun constructorExtractor(pattern: ConstructorPattern, path: PatternPath, env: StaticEnvironment, matchedObject: VariableReference): CoreExpression {
        val exps = ArrayList<CoreExpression?>(1 + pattern.args?.size().sure());

        var i = 0
        for (val p in pattern.args)
            exps.add(makeExtractor(p.sure(), path.extend(i++).sure(), env, matchedObject))

        return CoreSequenceExpression(exps)
    }

    private fun makePredicate(pattern: Pattern, path: PatternPath, matchedObject: VariableReference): CoreExpression =
        when (pattern) {
            is WildcardPattern    -> CoreConstantExpression(true)
            is VariablePattern    -> CoreConstantExpression(true)
            is LiteralPattern     -> CoreEqualConstantExpression(pattern.value, CoreExtractExpression(matchedObject, path))
            is ConstructorPattern -> constructorPredicate(pattern, path, matchedObject)
            else -> throw Exception()
        }

    private fun constructorPredicate(pattern: ConstructorPattern?, path: PatternPath, matchedObject: VariableReference): CoreExpression {
        val exps = ArrayList<CoreExpression?>(1 + pattern?.args?.size().sure());
        exps.add(matchesConstructor(path, pattern?.name.sure(), matchedObject));

        var i = 0
        for (val p in pattern?.args)
            exps.add(makePredicate(p.sure(), path.extend(i++).sure(), matchedObject))

        return CoreExpression.and(exps).sure()
    }

    private fun matchesConstructor(path: PatternPath, name: String, matchedObject: VariableReference): CoreExpression =
        CoreEqualConstantExpression(name, CoreExtractTagExpression(matchedObject, path))
}

