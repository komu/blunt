package komu.blunt.analyzer

import komu.blunt.ast.AST
import komu.blunt.ast.ASTAlternative
import komu.blunt.ast.ASTExpression
import komu.blunt.core.*
import komu.blunt.objects.TypeConstructorValue
import komu.blunt.types.DataTypeDefinitions
import komu.blunt.utils.Sequence

/**
 * Converts ASTExpressions to CoreExpressions.
 */
fun analyze(exp: ASTExpression, dataTypes: DataTypeDefinitions, env: StaticEnvironment): CoreExpression =
    Analyzer(dataTypes).run {
        renameIdentifiers(exp).simplify().analyze(env)
    }

class Analyzer(val dataTypes: DataTypeDefinitions) {

    private val sequence = Sequence()

    fun ASTExpression.analyze(env: StaticEnvironment): CoreExpression = when (this) {
        is ASTExpression.Constant       -> CoreConstantExpression(value)
        is ASTExpression.Variable       -> CoreVariableExpression(env[name])
        is ASTExpression.Set            -> CoreSetExpression(env[variable], exp.analyze(env))
        is ASTExpression.Sequence       -> CoreExpression.sequence(exps.map { it.analyze(env) })
        is ASTExpression.Application    -> CoreApplicationExpression(func.analyze(env), arg.analyze(env))
        is ASTExpression.Lambda         -> analyzeLambda(this, env)
        is ASTExpression.Let            -> analyzeLet(this, env)
        is ASTExpression.LetRec         -> analyzeLetRec(this, env)
        is ASTExpression.Constructor    -> analyzeConstructor(this, env)
        is ASTExpression.Case           -> analyzeCase(this, env)
    }

    private fun analyzeLambda(lambda: ASTExpression.Lambda, env: StaticEnvironment): CoreExpression {
        val newEnv = env.extend(lambda.argument)
        val body = lambda.body.analyze(newEnv)
        return CoreLambdaExpression(newEnv.size, body)
    }

    private fun analyzeConstructor(constructor: ASTExpression.Constructor, ctx: StaticEnvironment): CoreExpression {
        val ctor = dataTypes.findConstructor(constructor.name)

        return if (ctor.arity == 0)
            ASTExpression.Constant(TypeConstructorValue(ctor.index, ctor.name)).analyze(ctx)
        else
            ASTExpression.Variable(ctor.name).analyze(ctx)
    }

    private fun analyzeLet(let: ASTExpression.Let, env: StaticEnvironment): CoreExpression {
        val binding = let.bindings.singleOrNull() ?: throw UnsupportedOperationException("multi-var let is not supported")
        val v = env.define(binding.name)

        val expr = binding.expr.analyze(env)
        val body = let.body.analyze(env)

        return CoreLetExpression(v, expr, body)
    }

    private fun analyzeLetRec(let: ASTExpression.LetRec, env: StaticEnvironment): CoreExpression {
        val binding = let.bindings.singleOrNull() ?: throw UnsupportedOperationException("multi-var letrec is not supported")
        val v = env.define(binding.name)

        val expr = binding.expr.analyze(env)
        val body = let.body.analyze(env)

        return CoreLetExpression(v, expr, body)
    }

    private fun analyzeCase(case: ASTExpression.Case, env: StaticEnvironment): CoreExpression {
        val exp = case.exp.analyze(env)

        val matchedObject = env.define(sequence.nextSymbol("\$match"))
        val body = createAlts(matchedObject, case.alternatives, env)

        return CoreLetExpression(matchedObject, exp, body)
    }

    private fun createAlts(matchedObject: VariableReference, alts: List<ASTAlternative>, env: StaticEnvironment): CoreExpression =
        if (alts.isEmpty())
            AST.error("match failure").analyze(env)
        else {
            val first = alts.first()
            val predicate = PatternAnalyzer.makePredicate(first.pattern, matchedObject)
            val extractor = PatternAnalyzer.makeExtractor(first.pattern, matchedObject, env)
            val body = CoreExpression.sequence(extractor, first.value.analyze(env))
            val rest = createAlts(matchedObject, alts.drop(1), env)
            CoreIfExpression(predicate, body, rest)
        }
}
