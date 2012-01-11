package komu.blunt.analyzer;

import komu.blunt.ast.*;
import komu.blunt.core.*;
import komu.blunt.objects.Symbol;
import komu.blunt.objects.TypeConstructorValue;
import komu.blunt.types.ConstructorDefinition;
import komu.blunt.types.DataTypeDefinitions;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static komu.blunt.objects.Symbol.symbol;

final class AnalyzingVisitor implements ASTVisitor<StaticEnvironment, CoreExpression> {

    private final DataTypeDefinitions dataTypes;
    private final PatternAnalyzer patternAnalyzer = new PatternAnalyzer();
    private int sequence = 1;

    public AnalyzingVisitor(DataTypeDefinitions dataTypes) {
        this.dataTypes = checkNotNull(dataTypes);
    }

    public CoreExpression analyze(ASTExpression exp, StaticEnvironment env) {
        return exp.accept(this, env);
    }

    private List<CoreExpression> analyzeAll(List<ASTExpression> exps, StaticEnvironment env) {
        List<CoreExpression> result = new ArrayList<>(exps.size());

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
    public CoreExpression visit(ASTLambda lambda, StaticEnvironment env) {
        StaticEnvironment newEnv = env.extend(lambda.argument);
        CoreExpression body = analyze(lambda.body, newEnv);
        return new CoreLambdaExpression(newEnv.size(), body);
    }

    @Override
    public CoreExpression visit(ASTLet let, StaticEnvironment env) {
        if (let.bindings.size() != 1)
            throw new UnsupportedOperationException("multi-var let is not supported");

        ImplicitBinding binding = let.bindings.get(0);
        VariableReference var = env.define(binding.name);

        CoreExpression expr = analyze(binding.expr, env);
        CoreExpression body = analyze(let.body, env);
        return new CoreLetExpression(var, expr, body);
    }

    @Override
    public CoreExpression visit(ASTLetRec let, StaticEnvironment env) {
        if (let.bindings.size() != 1)
            throw new UnsupportedOperationException("multi-var let is not supported");

        ImplicitBinding binding = let.bindings.get(0);
        VariableReference var = env.define(binding.name);

        CoreExpression expr = analyze(binding.expr, env);
        CoreExpression body = analyze(let.body, env);
        return new CoreLetExpression(var, expr, body);
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
    public CoreExpression visit(ASTVariable variable, StaticEnvironment env) {
        return new CoreVariableExpression(env.lookup(variable.var));
    }

    @Override
    public CoreExpression visit(ASTConstructor constructor, StaticEnvironment ctx) {
        ConstructorDefinition ctor = dataTypes.findConstructor(constructor.name);

        if (ctor.arity == 0)
            return AST.constant(new TypeConstructorValue(ctor.index, ctor.name)).accept(this, ctx);
        else
            return AST.variable(ctor.name).accept(this, ctx);
    }

    @Override
    public CoreExpression visit(ASTCase astCase, StaticEnvironment env) {
        CoreExpression exp = analyze(astCase.exp, env);

        Symbol var = symbol("$match" + sequence++);
        VariableReference matchedObject = env.define(var);
        CoreExpression body = createAlts(matchedObject, astCase.alternatives, env);
        return new CoreLetExpression(matchedObject, exp, body);
    }
    
    private CoreExpression createAlts(VariableReference matchedObject, List<ASTAlternative> alts, StaticEnvironment env) {
        if (alts.isEmpty())
            return analyze(AST.apply(AST.variable("error"), AST.constant("match failure")), env);
            
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
