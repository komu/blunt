package komu.blunt.analyzer

import komu.blunt.core.*
import komu.blunt.types.patterns.Pattern
import java.util.*

object PatternAnalyzer {

    fun makeExtractor(pattern: Pattern, matchedObject: VariableReference, env: StaticEnvironment, path: PatternPath=PatternPath.EMPTY): CoreExpression = when (pattern) {
        is Pattern.Wildcard     -> CoreExpression.EMPTY
        is Pattern.Literal      -> CoreExpression.EMPTY
        is Pattern.Variable     -> variableExtractor(pattern, path, env, matchedObject)
        is Pattern.Constructor  -> constructorExtractor(pattern, path, env, matchedObject)
    }

    private fun variableExtractor(pattern: Pattern.Variable, path: PatternPath, env: StaticEnvironment, matchedObject: VariableReference): CoreExpression {
        val v = env.lookupInCurrentScopeOrDefine(pattern.variable)
        return CoreSetExpression(v, CoreExtractExpression(matchedObject, path))
    }

    private fun constructorExtractor(pattern: Pattern.Constructor, path: PatternPath, env: StaticEnvironment, matchedObject: VariableReference): CoreExpression =
        CoreExpression.sequence(pattern.args.mapIndexed { i, p ->
            makeExtractor(p, matchedObject, env, path.extend(i))
        })

    fun makePredicate(pattern: Pattern, matchedObject: VariableReference, path: PatternPath=PatternPath.EMPTY): CoreExpression = when (pattern) {
        is Pattern.Wildcard     -> CoreExpression.TRUE
        is Pattern.Variable     -> CoreExpression.TRUE
        is Pattern.Literal      -> CoreEqualConstantExpression(pattern.value, CoreExtractExpression(matchedObject, path))
        is Pattern.Constructor  -> constructorPredicate(pattern, path, matchedObject)
    }

    private fun constructorPredicate(pattern: Pattern.Constructor, path: PatternPath, matchedObject: VariableReference): CoreExpression {
        val exps = ArrayList<CoreExpression>()
        exps.add(matchesConstructor(path, pattern.name, matchedObject))

        pattern.args.forEachIndexed { i, p ->
            exps.add(makePredicate(p, matchedObject, path.extend(i)))
        }

        return CoreExpression.and(exps)
    }

    private fun matchesConstructor(path: PatternPath, name: String, matchedObject: VariableReference): CoreExpression =
        CoreEqualConstantExpression(name, CoreExtractTagExpression(matchedObject, path))
}
