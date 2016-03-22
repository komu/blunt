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
    Analyzer(dataTypes).analyze(renameIdentifiers(exp).simplify(), env)

class Analyzer(val dataTypes: DataTypeDefinitions) {

    private val sequence = Sequence()

    fun analyze(exp: ASTExpression, env: StaticEnvironment): CoreExpression =
      when (exp) {
          is ASTExpression.Constant -> CoreConstantExpression(exp.value)
          is ASTExpression.Variable -> CoreVariableExpression(env[exp.name])
          is ASTExpression.Set -> CoreSetExpression(env[exp.variable], analyze(exp.exp, env))
          is ASTExpression.Sequence -> CoreExpression.sequence(exp.exps.map { analyze(it, env) })
          is ASTExpression.Application -> CoreApplicationExpression(analyze(exp.func, env), analyze(exp.arg, env))
          is ASTExpression.Lambda -> analyzeLambda(exp, env)
          is ASTExpression.Let -> analyzeLet(exp, env)
          is ASTExpression.LetRec -> analyzeLetRec(exp, env)
          is ASTExpression.Constructor -> analyzeConstructor(exp, env)
          is ASTExpression.Case -> analyzeCase(exp, env)
      }

    private fun analyzeLambda(lambda: ASTExpression.Lambda, env: StaticEnvironment): CoreExpression {
        val newEnv = env.extend(lambda.argument)
        val body = analyze(lambda.body, newEnv)
        return CoreLambdaExpression(newEnv.size, body)
    }

    private fun analyzeConstructor(constructor: ASTExpression.Constructor, ctx: StaticEnvironment): CoreExpression {
        val ctor = dataTypes.findConstructor(constructor.name)

        return if (ctor.arity == 0)
            analyze(ASTExpression.Constant(TypeConstructorValue(ctor.index, ctor.name)), ctx)
        else
            analyze(ASTExpression.Variable(ctor.name), ctx)
    }

    private fun analyzeLet(let: ASTExpression.Let, env: StaticEnvironment): CoreExpression {
        if (let.bindings.size != 1)
            throw UnsupportedOperationException("multi-var let is not supported")

        val binding = let.bindings.first()
        val v = env.define(binding.name)

        val expr = analyze(binding.expr, env)
        val body = analyze(let.body, env)

        return CoreLetExpression(v, expr, body)
    }

    private fun analyzeLetRec(let: ASTExpression.LetRec, env: StaticEnvironment): CoreExpression {
        if (let.bindings.size != 1)
            throw UnsupportedOperationException("multi-var letrec is not supported")

        val binding = let.bindings.first()
        val v = env.define(binding.name)

        val expr = analyze(binding.expr, env)
        val body = analyze(let.body, env)

        return CoreLetExpression(v, expr, body)
    }

    private fun analyzeCase(case: ASTExpression.Case, env: StaticEnvironment): CoreExpression {
        val exp = analyze(case.exp, env)

        val matchedObject = env.define(sequence.nextSymbol("\$match"))
        val body = createAlts(matchedObject, case.alternatives, env)

        return CoreLetExpression(matchedObject, exp, body)
    }

    private fun createAlts(matchedObject: VariableReference, alts: List<ASTAlternative>, env: StaticEnvironment): CoreExpression =
        if (alts.isEmpty())
            analyze(AST.error("match failure"), env)
        else {
            val first = alts.first()
            val predicate = PatternAnalyzer.makePredicate(first.pattern, matchedObject)
            val extractor = PatternAnalyzer.makeExtractor(first.pattern, matchedObject, env)
            val body = CoreExpression.sequence(extractor, analyze(first.value, env))
            val rest = createAlts(matchedObject, alts.drop(1), env)
            CoreIfExpression(predicate, body, rest)
        }
}
