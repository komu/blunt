package komu.blunt.analyzer

import komu.blunt.ast.*
import komu.blunt.core.*
import komu.blunt.objects.Symbol
import komu.blunt.objects.TypeConstructorValue
import komu.blunt.types.ConstructorDefinition
import komu.blunt.types.DataTypeDefinitions

import java.util.ArrayList
import java.util.List

import komu.blunt.objects.Symbol.symbol

class AnalyzingVisitor(val dataTypes: DataTypeDefinitions) : ASTVisitor<StaticEnvironment, CoreExpression> {

    private val patternAnalyzer = PatternAnalyzer()
    private var sequence = 1

    fun analyze(exp: ASTExpression?, env: StaticEnvironment): CoreExpression =
        exp.accept(this, env)

    private fun analyzeAll(exps: List<ASTExpression?>?, env: StaticEnvironment): List<CoreExpression?>  {
        val result = ArrayList<CoreExpression?>(exps?.size().sure())

        for (val exp in exps)
            result.add(analyze(exp, env))

        return result
    }

    override fun visit(lambda: ASTLambda?, env: StaticEnvironment): CoreExpression {
        val newEnv = env.extend(lambda?.argument).sure()
        val body = analyze(lambda?.body, newEnv)
        return CoreLambdaExpression(newEnv.size(), body)
    }

    override fun visit(application: ASTApplication?, env: StaticEnvironment): CoreExpression =
        CoreApplicationExpression(analyze(application?.func, env), analyze(application?.arg, env))

    override fun visit(constructor: ASTConstructor?, ctx: StaticEnvironment): CoreExpression {
        val ctor = dataTypes.findConstructor(constructor?.name).sure()

        if (ctor.arity == 0)
            return AST.constant(TypeConstructorValue(ctor.index, ctor.name)).accept(this, ctx).sure()
        else
            return AST.variable(ctor.name).accept(this, ctx).sure()
    }

    override fun visit(sequence: ASTSequence?, env: StaticEnvironment): CoreExpression {
        if (sequence?.exps.sure().isEmpty()) throw AnalyzationException("empty sequence")

        return CoreSequenceExpression(analyzeAll(sequence?.exps, env))
    }

    override fun visit(set: ASTSet?, env: StaticEnvironment): CoreExpression =
        CoreSetExpression(env.lookup(set?.`var`), analyze(set?.exp, env))

    override fun visit(constant: ASTConstant?, ctx: StaticEnvironment): CoreExpression =
        CoreConstantExpression(constant?.value)

    override fun visit(let: ASTLet?, env: StaticEnvironment): CoreExpression {
        if (let?.bindings?.size() != 1)
            throw UnsupportedOperationException("multi-var let is not supported")

        val binding = let?.bindings?.get(0)
        val v = env.define(binding?.name)

        val expr = analyze(binding?.expr, env)
        val body = analyze(let?.body, env)
        return CoreLetExpression(v, expr, body)
    }

    override fun visit(let: ASTLetRec?, env: StaticEnvironment): CoreExpression {
        if (let?.bindings?.size() != 1)
            throw UnsupportedOperationException("multi-var let is not supported")

        val binding = let?.bindings?.get(0)
        val v = env.define(binding?.name)

        val expr = analyze(binding?.expr, env)
        val body = analyze(let?.body, env)

        return CoreLetExpression(v, expr, body)
    }

    override fun visit(variable: ASTVariable?, env: StaticEnvironment): CoreExpression =
        CoreVariableExpression(env.lookup(variable?.`var`))

    override fun visit(astCase: ASTCase?, env: StaticEnvironment): CoreExpression {
        val exp = analyze(astCase?.exp, env)

        val v = symbol("\$match" + sequence++)
        val matchedObject = env.define(v).sure()
        val body = createAlts(matchedObject, astCase?.alternatives.sure(), env)
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

