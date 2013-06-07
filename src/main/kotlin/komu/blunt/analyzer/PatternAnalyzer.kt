package komu.blunt.analyzer

import komu.blunt.core.*
import komu.blunt.types.patterns.*

object PatternAnalyzer {

    fun makeExtractor(pattern: Pattern, matchedObject: VariableReference, env: StaticEnvironment, path: PatternPath=PatternPath.EMPTY): CoreExpression =
        when (pattern) {
            is WildcardPattern    -> CoreExpression.EMPTY
            is LiteralPattern     -> CoreExpression.EMPTY
            is VariablePattern    -> variableExtractor(pattern, path, env, matchedObject)
            is ConstructorPattern -> constructorExtractor(pattern, path, env, matchedObject)
            else                  -> throw AnalyzationException("unnown pattern $pattern")
        }

    private fun variableExtractor(pattern: VariablePattern, path: PatternPath, env: StaticEnvironment, matchedObject: VariableReference): CoreExpression {
        val v = env.lookupInCurrentScopeOrDefine(pattern.variable)
        return CoreSetExpression(v, CoreExtractExpression(matchedObject, path))
    }

    private fun constructorExtractor(pattern: ConstructorPattern, path: PatternPath, env: StaticEnvironment, matchedObject: VariableReference): CoreExpression =
        CoreExpression.sequence(pattern.args.withIndices().map { p ->
            makeExtractor(p.second, matchedObject, env, path.extend(p.first))
        }.toList())

    fun makePredicate(pattern: Pattern, matchedObject: VariableReference, path: PatternPath=PatternPath.EMPTY): CoreExpression =
        when (pattern) {
            is WildcardPattern    -> CoreExpression.TRUE
            is VariablePattern    -> CoreExpression.TRUE
            is LiteralPattern     -> CoreEqualConstantExpression(pattern.value, CoreExtractExpression(matchedObject, path))
            is ConstructorPattern -> constructorPredicate(pattern, path, matchedObject)
            else                  -> throw AnalyzationException("unknown pattern '$pattern'")
        }

    private fun constructorPredicate(pattern: ConstructorPattern, path: PatternPath, matchedObject: VariableReference): CoreExpression {
        val exps = listBuilder<CoreExpression>()
        exps.add(matchesConstructor(path, pattern.name, matchedObject))

        for ((i,p) in pattern.args.withIndices())
            exps.add(makePredicate(p, matchedObject, path.extend(i)))

        return CoreExpression.and(exps.build())
    }

    private fun matchesConstructor(path: PatternPath, name: String, matchedObject: VariableReference): CoreExpression =
        CoreEqualConstantExpression(name, CoreExtractTagExpression(matchedObject, path))
}
