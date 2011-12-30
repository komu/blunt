package komu.blunt.analyzer;

import komu.blunt.ast.*;
import komu.blunt.core.*;
import komu.blunt.objects.Symbol;
import komu.blunt.types.ConstructorDefinition;
import komu.blunt.types.DataTypeDefinitions;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static komu.blunt.objects.Symbol.symbol;

final class AnalyzingVisitor implements ASTVisitor<StaticEnvironment, CoreExpression> {

    private final DataTypeDefinitions dataTypes;
    private final PatternAnalyzer patternAnalyzer = new PatternAnalyzer();

    public AnalyzingVisitor(DataTypeDefinitions dataTypes) {
        this.dataTypes = checkNotNull(dataTypes);
    }

    public CoreExpression analyze(ASTExpression exp, StaticEnvironment env) {
        return exp.accept(this, env);
    }

    private List<CoreExpression> analyzeAll(List<ASTExpression> exps, StaticEnvironment env) {
        List<CoreExpression> result = new ArrayList<CoreExpression>(exps.size());

        for (ASTExpression exp : exps)
            result.add(analyze(exp, env));

        return result;
    }

    @Override
    public CoreExpression visit(ASTApplication application, StaticEnvironment env) {
        return new CoreApplicationExpression(analyze(application.func, env), analyze(application.arg, env));
    }

    @Override
    public CoreExpression visit(ASTConstant constant, StaticEnvironment env) {
        return new CoreConstantExpression(constant.value);
    }

    @Override
    public CoreExpression visit(ASTIf ifExp, StaticEnvironment env) {
        return new CoreIfExpression(analyze(ifExp.test, env), analyze(ifExp.consequent, env), analyze(ifExp.alternative, env));
    }

    @Override
    public CoreExpression visit(ASTLambda lambda, StaticEnvironment env) {
        if (lambda.arguments.size() == 1) {
            Symbol arg = lambda.arguments.get(0);
            StaticEnvironment newEnv = env.extend(arg);
            return new CoreLambdaExpression(arg, analyze(lambda.body, newEnv));
        } else {
            return analyze(lambda.rewrite(), env);
        }
    }

    @Override
    public CoreExpression visit(ASTLet let, StaticEnvironment env) {
        if (let.bindings.size() != 1)
            throw new UnsupportedOperationException("multi-var let is not supported");

        StaticEnvironment newEnv = env.extend(let.getVariables());

        ImplicitBinding binding = let.bindings.get(0);

        return new CoreLetExpression(binding.name, analyze(binding.expr, env), analyze(let.body, newEnv));
    }

    @Override
    public CoreExpression visit(ASTLetRec letRec, StaticEnvironment env) {
        return analyze(letRec.rewriteToLet(), env);
    }

    @Override
    public CoreExpression visit(ASTSequence sequence, StaticEnvironment env) {
        if (sequence.exps.isEmpty()) throw new AnalyzationException("empty sequence");

        return new CoreSequenceExpression(analyzeAll(sequence.exps, env));
    }

    @Override
    public CoreExpression visit(ASTSet set, StaticEnvironment env) {
        return new CoreSetExpression(env.lookup(set.var), analyze(set.exp, env));
    }

    @Override
    public CoreExpression visit(ASTTuple tuple, StaticEnvironment env) {
        String name = DataTypeDefinitions.tupleName(tuple.exps.size());
        return new CoreConstructorExpression(name, analyzeAll(tuple.exps, env));
    }

    @Override
    public CoreExpression visit(ASTVariable variable, StaticEnvironment env) {
        return new CoreVariableExpression(env.lookup(variable.var));
    }

    @Override
    public CoreExpression visit(ASTList list, StaticEnvironment env) {
        return analyze(list.rewrite(), env);
    }

    @Override
    public CoreExpression visit(ASTConstructor constructor, StaticEnvironment ctx) {
        ConstructorDefinition ctor = dataTypes.findConstructor(constructor.name);

        return new ASTVariable(ctor.name).accept(this, ctx);
    }

    @Override
    public CoreExpression visit(ASTCase astCase, StaticEnvironment env) {
        CoreExpression exp = analyze(astCase.exp, env);

        StaticEnvironment newEnv = new StaticEnvironment(env);

        Symbol var = symbol("$match"); // TODO: fresh name
        VariableReference matchedObject = newEnv.define(var);
        return new CoreLetExpression(var, exp, createAlts(matchedObject, astCase.alternatives, newEnv));
    }
    
    private CoreExpression createAlts(VariableReference matchedObject, List<ASTAlternative> alts, StaticEnvironment env) {
        if (alts.isEmpty())
            return analyze(new ASTApplication(new ASTVariable("error"), new ASTConstant("match failure")), env);
            
        ASTAlternative head = alts.get(0);
        List<ASTAlternative> tail = alts.subList(1, alts.size());

        CoreAlternative alt = analyze(head, matchedObject, env);
        return new CoreIfExpression(alt.extractor, alt.body, createAlts(matchedObject, tail, env));
    }

    private CoreAlternative analyze(ASTAlternative alt, VariableReference matchedObject, StaticEnvironment env) {
        CoreExpression extractor = patternAnalyzer.createExtractor(alt.pattern, matchedObject, env);
        CoreExpression body = analyze(alt.value, env);
        return new CoreAlternative(extractor, body);
    }
}
