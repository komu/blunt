package komu.blunt.analyzer

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

    fun analyze(exp: ASTExpression?, ctx: StaticEnvironment): CoreExpression =
      when (exp) {
          is ASTApplication -> visit(exp, ctx)
          is ASTConstant    -> visit(exp, ctx)
          is ASTLambda      -> visit(exp, ctx)
          is ASTLet         -> visit(exp, ctx)
          is ASTLetRec      -> visit(exp, ctx)
          is ASTSequence    -> visit(exp, ctx)
          is ASTSet         -> visit(exp, ctx)
          is ASTVariable    -> visit(exp, ctx)
          is ASTConstructor -> visit(exp, ctx)
          is ASTCase        -> visit(exp, ctx)
          else              -> throw Exception("unknown exp $exp")
      }

    private fun analyzeAll(exps: List<ASTExpression?>?, env: StaticEnvironment): List<CoreExpression?>  {
        val result = ArrayList<CoreExpression?>(exps?.size().sure())

        for (val exp in exps)
            result.add(analyze(exp, env))

        return result
    }

    private fun visit(lambda: ASTLambda, env: StaticEnvironment): CoreExpression {
        val newEnv = env.extend(lambda.argument)
        val body = analyze(lambda.body, newEnv)
        return CoreLambdaExpression(newEnv.size(), body)
    }

    private fun visit(application: ASTApplication, env: StaticEnvironment): CoreExpression =
        CoreApplicationExpression(analyze(application.func, env), analyze(application.arg, env))

    private fun visit(constructor: ASTConstructor, ctx: StaticEnvironment): CoreExpression {
        val ctor = dataTypes.findConstructor(constructor.name).sure()

        if (ctor.arity == 0)
            return analyze(AST.constant(TypeConstructorValue(ctor.index, ctor.name)), ctx)
        else
            return analyze(AST.variable(ctor.name.sure()), ctx)
    }

    private fun visit(sequence: ASTSequence, env: StaticEnvironment): CoreExpression {
        if (sequence.exps.sure().isEmpty()) throw AnalyzationException("empty sequence")

        return CoreSequenceExpression(analyzeAll(sequence.exps, env))
    }

    private fun visit(set: ASTSet, env: StaticEnvironment): CoreExpression =
        CoreSetExpression(env.lookup(set.variable), analyze(set.exp, env))

    private fun visit(constant: ASTConstant, ctx: StaticEnvironment): CoreExpression =
        CoreConstantExpression(constant.value)

    private fun visit(let: ASTLet, env: StaticEnvironment): CoreExpression {
        if (let.bindings.size() != 1)
            throw UnsupportedOperationException("multi-var let is not supported")

        val binding = let.bindings.get(0).sure()
        val v = env.define(binding.name)

        val expr = analyze(binding.expr, env)
        val body = analyze(let.body, env)
        return CoreLetExpression(v, expr, body)
    }

    private fun visit(let: ASTLetRec, env: StaticEnvironment): CoreExpression {
        if (let.bindings.size() != 1)
            throw UnsupportedOperationException("multi-var let is not supported")

        val binding = let.bindings.get(0).sure()
        val v = env.define(binding.name)

        val expr = analyze(binding.expr, env)
        val body = analyze(let.body, env)

        return CoreLetExpression(v, expr, body)
    }

    private fun visit(variable: ASTVariable, env: StaticEnvironment): CoreExpression =
        CoreVariableExpression(env.lookup(variable.name))

    private fun visit(astCase: ASTCase, env: StaticEnvironment): CoreExpression {
        val exp = analyze(astCase.exp, env)

        val v = Symbol("\$match" + sequence++)
        val matchedObject = env.define(v).sure()
        val body = createAlts(matchedObject, astCase.alternatives, env)
        return CoreLetExpression(matchedObject, exp, body)
    }

    private fun createAlts(matchedObject: VariableReference, alts: List<ASTAlternative?>, env: StaticEnvironment): CoreExpression {
        if (alts.isEmpty())
            return analyze(AST.apply(AST.variable("error"), AST.constant("match failure")), env)

        val head = alts.get(0).sure()
        val tail = alts.subList(1, alts.size()).sure()

        val alt = analyze(head, matchedObject, env)
        return CoreIfExpression(alt.extractor, alt.body, createAlts(matchedObject, tail, env))
    }

    private fun analyze(alt: ASTAlternative, matchedObject: VariableReference, env: StaticEnvironment): CoreAlternative {
        val extractor = patternAnalyzer.createExtractor(alt.pattern.sure(), matchedObject, env).sure()
        val body = analyze(alt.value, env)
        return CoreAlternative(extractor.predicate, CoreSequenceExpression(extractor.extractor, body))
    }
}
