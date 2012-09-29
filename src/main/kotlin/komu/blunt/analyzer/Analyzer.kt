package komu.blunt.analyzer

import komu.blunt.ast.*
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
          is ASTConstant    -> CoreConstantExpression(exp.value)
          is ASTVariable    -> CoreVariableExpression(env[exp.name])
          is ASTSet         -> CoreSetExpression(env[exp.variable], analyze(exp.exp, env))
          is ASTSequence    -> CoreExpression.sequence(exp.exps.map { analyze(it, env) })
          is ASTApplication -> CoreApplicationExpression(analyze(exp.func, env), analyze(exp.arg, env))
          is ASTLambda      -> analyzeLambda(exp, env)
          is ASTLet         -> analyzeLet(exp, env)
          is ASTLetRec      -> analyzeLetRec(exp, env)
          is ASTConstructor -> analyzeConstructor(exp, env)
          is ASTCase        -> analyzeCase(exp, env)
          else              -> throw AnalyzationException("unknown expression '$exp'")
      }

    private fun analyzeLambda(lambda: ASTLambda, env: StaticEnvironment): CoreExpression {
        val newEnv = env.extend(lambda.argument)
        val body = analyze(lambda.body, newEnv)
        return CoreLambdaExpression(newEnv.size, body)
    }

    private fun analyzeConstructor(constructor: ASTConstructor, ctx: StaticEnvironment): CoreExpression {
        val ctor = dataTypes.findConstructor(constructor.name)

        return if (ctor.arity == 0)
            analyze(AST.constant(TypeConstructorValue(ctor.index, ctor.name)), ctx)
        else
            analyze(AST.variable(ctor.name), ctx)
    }

    private fun analyzeLet(let: ASTLet, env: StaticEnvironment): CoreExpression {
        if (let.bindings.size != 1)
            throw UnsupportedOperationException("multi-var let is not supported")

        val binding = let.bindings.first()
        val v = env.define(binding.name)

        val expr = analyze(binding.expr, env)
        val body = analyze(let.body, env)

        return CoreLetExpression(v, expr, body)
    }

    private fun analyzeLetRec(let: ASTLetRec, env: StaticEnvironment): CoreExpression {
        if (let.bindings.size != 1)
            throw UnsupportedOperationException("multi-var letrec is not supported")

        val binding = let.bindings.first()
        val v = env.define(binding.name)

        val expr = analyze(binding.expr, env)
        val body = analyze(let.body, env)

        return CoreLetExpression(v, expr, body)
    }

    private fun analyzeCase(astCase: ASTCase, env: StaticEnvironment): CoreExpression {
        val exp = analyze(astCase.exp, env)

        val matchedObject = env.define(sequence.nextSymbol("\$match"))
        val body = createAlts(matchedObject, astCase.alternatives, env)

        return CoreLetExpression(matchedObject, exp, body)
    }

    private fun createAlts(matchedObject: VariableReference, alts: List<ASTAlternative>, env: StaticEnvironment): CoreExpression =
        if (alts.empty)
            analyze(AST.error("match failure"), env)
        else {
            val first = alts.first()
            val predicate = PatternAnalyzer.makePredicate(first.pattern, matchedObject)
            val extractor = PatternAnalyzer.makeExtractor(first.pattern, matchedObject, env)
            val body = CoreExpression.sequence(extractor, analyze(first.value, env))
            val rest = createAlts(matchedObject, alts.tail, env)
            CoreIfExpression(predicate, body, rest)
        }
}
