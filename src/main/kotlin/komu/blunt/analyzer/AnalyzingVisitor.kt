package komu.blunt.analyzer

import kotlin.util.*

import komu.blunt.ast.*
import komu.blunt.core.*
import komu.blunt.objects.Symbol
import komu.blunt.objects.TypeConstructorValue
import komu.blunt.types.ConstructorDefinition
import komu.blunt.types.DataTypeDefinitions

import java.util.ArrayList
import java.util.List

class AnalyzingVisitor(val dataTypes: DataTypeDefinitions) {

    private val patternAnalyzer = PatternAnalyzer()
    private var sequence = 1

    fun analyze(exp: ASTExpression, env: StaticEnvironment): CoreExpression =
      when (exp) {
          is ASTConstant    -> CoreConstantExpression(exp.value)
          is ASTSet         -> CoreSetExpression(env.lookup(exp.variable), analyze(exp.exp, env))
          is ASTApplication -> visit(exp, env)
          is ASTLambda      -> visit(exp, env)
          is ASTLet         -> visit(exp, env)
          is ASTLetRec      -> visit(exp, env)
          is ASTSequence    -> visit(exp, env)
          is ASTVariable    -> visit(exp, env)
          is ASTConstructor -> visit(exp, env)
          is ASTCase        -> visit(exp, env)
          else              -> throw Exception("unknown exp $exp")
      }

    private fun analyzeAll(exps: List<ASTExpression>, env: StaticEnvironment): List<CoreExpression> =
        exps.map { analyze(it, env) }

    private fun visit(lambda: ASTLambda, env: StaticEnvironment): CoreExpression {
        val newEnv = env.extend(lambda.argument)
        val body = analyze(lambda.body, newEnv)
        return CoreLambdaExpression(newEnv.size, body)
    }

    private fun visit(application: ASTApplication, env: StaticEnvironment): CoreExpression =
        CoreApplicationExpression(analyze(application.func, env), analyze(application.arg, env))

    private fun visit(constructor: ASTConstructor, ctx: StaticEnvironment): CoreExpression {
        val ctor = dataTypes.findConstructor(constructor.name)

        if (ctor.arity == 0)
            return analyze(AST.constant(TypeConstructorValue(ctor.index, ctor.name)), ctx)
        else
            return analyze(AST.variable(ctor.name), ctx)
    }

    private fun visit(sequence: ASTSequence, env: StaticEnvironment): CoreExpression {
        if (sequence.exps.isEmpty()) throw AnalyzationException("empty sequence")

        return CoreSequenceExpression(analyzeAll(sequence.exps, env))
    }

    private fun visit(let: ASTLet, env: StaticEnvironment): CoreExpression {
        if (let.bindings.size != 1)
            throw UnsupportedOperationException("multi-var let is not supported")

        val binding = let.bindings.first()
        val v = env.define(binding.name)

        val expr = analyze(binding.expr, env)
        val body = analyze(let.body, env)

        return CoreLetExpression(v, expr, body)
    }

    private fun visit(let: ASTLetRec, env: StaticEnvironment): CoreExpression {
        if (let.bindings.size != 1)
            throw UnsupportedOperationException("multi-var letrec is not supported")

        val binding = let.bindings.first()
        val v = env.define(binding.name)

        val expr = analyze(binding.expr, env)
        val body = analyze(let.body, env)

        return CoreLetExpression(v, expr, body)
    }

    private fun visit(variable: ASTVariable, env: StaticEnvironment): CoreExpression =
        CoreVariableExpression(env.lookup(variable.name))

    private fun visit(astCase: ASTCase, env: StaticEnvironment): CoreExpression {
        val exp = analyze(astCase.exp, env)

        val v = Symbol("\$match${sequence++}")
        val matchedObject = env.define(v)
        val body = createAlts(matchedObject, astCase.alternatives, env)
        return CoreLetExpression(matchedObject, exp, body)
    }

    private fun createAlts(matchedObject: VariableReference, alts: List<ASTAlternative>, env: StaticEnvironment): CoreExpression {
        if (alts.isEmpty())
            return analyze(AST.apply(AST.variable("error"), AST.constant("match failure")), env)

        val head = alts.first()
        val tail = alts.subList(1, alts.size)

        val alt = analyze(head, matchedObject, env)
        return CoreIfExpression(alt.extractor, alt.body, createAlts(matchedObject, tail, env))
    }

    private fun analyze(alt: ASTAlternative, matchedObject: VariableReference, env: StaticEnvironment): CoreAlternative {
        val extractor = patternAnalyzer.createExtractor(alt.pattern, matchedObject, env)
        val body = analyze(alt.value, env)
        return CoreAlternative(extractor.predicate, CoreSequenceExpression(extractor.extractor, body))
    }
}
