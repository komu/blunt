package komu.blunt.analyzer

import komu.blunt.core.*
import komu.blunt.types.patterns.*

class PatternAnalyzer {

    fun createExtractor(pattern: Pattern, matchedObject: VariableReference, env: StaticEnvironment): PatternExtractor {
        val predicate = makePredicate(pattern, PatternPath.EMPTY, matchedObject)
        val extractor = makeExtractor(pattern, PatternPath.EMPTY, env, matchedObject)
        return PatternExtractor(predicate, extractor)
    }

    private fun makeExtractor(pattern: Pattern, path: PatternPath, env: StaticEnvironment, matchedObject: VariableReference): CoreExpression =
        when (pattern) {
            is WildcardPattern    -> CoreEmptyExpression.INSTANCE
            is LiteralPattern     -> CoreEmptyExpression.INSTANCE
            is VariablePattern    -> variableExtractor(pattern, path, env, matchedObject)
            is ConstructorPattern -> constructorExtractor(pattern, path, env, matchedObject)
            else                  -> throw AnalyzationException("unnown pattern $pattern")
        }

    private fun variableExtractor(pattern: VariablePattern, path: PatternPath, env: StaticEnvironment, matchedObject: VariableReference): CoreExpression {
        val v = env.lookupInCurrentScopeOrDefine(pattern.variable)
        return CoreSetExpression(v, CoreExtractExpression(matchedObject, path))
    }

    private fun constructorExtractor(pattern: ConstructorPattern, path: PatternPath, env: StaticEnvironment, matchedObject: VariableReference): CoreExpression {
        val exps = listBuilder<CoreExpression>()

        for ((i,p) in pattern.args.withIndices())
            exps.add(makeExtractor(p, path.extend(i), env, matchedObject))

        return CoreSequenceExpression(exps.build())
    }

    private fun makePredicate(pattern: Pattern, path: PatternPath, matchedObject: VariableReference): CoreExpression =
        when (pattern) {
            is WildcardPattern    -> CoreConstantExpression(true)
            is VariablePattern    -> CoreConstantExpression(true)
            is LiteralPattern     -> CoreEqualConstantExpression(pattern.value, CoreExtractExpression(matchedObject, path))
            is ConstructorPattern -> constructorPredicate(pattern, path, matchedObject)
            else                  -> throw AnalyzationException("unknown pattern '$pattern'")
        }

    private fun constructorPredicate(pattern: ConstructorPattern, path: PatternPath, matchedObject: VariableReference): CoreExpression {
        val exps = listBuilder<CoreExpression>()
        exps.add(matchesConstructor(path, pattern.name, matchedObject))

        for ((i,p) in pattern.args.withIndices())
            exps.add(makePredicate(p, path.extend(i), matchedObject))

        return CoreExpression.and(exps.build())
    }

    private fun matchesConstructor(path: PatternPath, name: String, matchedObject: VariableReference): CoreExpression =
        CoreEqualConstantExpression(name, CoreExtractTagExpression(matchedObject, path))
}
