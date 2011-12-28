package komu.blunt.analyzer;

import java.util.ArrayList;
import java.util.List;

import komu.blunt.ast.ASTApplication;
import komu.blunt.ast.ASTConstant;
import komu.blunt.ast.ASTConstructor;
import komu.blunt.ast.ASTExpression;
import komu.blunt.ast.ASTIf;
import komu.blunt.ast.ASTLambda;
import komu.blunt.ast.ASTLet;
import komu.blunt.ast.ASTLetRec;
import komu.blunt.ast.ASTList;
import komu.blunt.ast.ASTSequence;
import komu.blunt.ast.ASTSet;
import komu.blunt.ast.ASTTuple;
import komu.blunt.ast.ASTVariable;
import komu.blunt.ast.ASTVisitor;
import komu.blunt.ast.ImplicitBinding;
import komu.blunt.core.CoreApplicationExpression;
import komu.blunt.core.CoreConstantExpression;
import komu.blunt.core.CoreConstructorExpression;
import komu.blunt.core.CoreExpression;
import komu.blunt.core.CoreIfExpression;
import komu.blunt.core.CoreLambdaExpression;
import komu.blunt.core.CoreLetExpression;
import komu.blunt.core.CoreSequenceExpression;
import komu.blunt.core.CoreSetExpression;
import komu.blunt.core.CoreVariableExpression;
import komu.blunt.objects.Symbol;
import komu.blunt.types.ConstructorDefinition;
import komu.blunt.types.DataTypeDefinitions;

public final class AnalyzingVisitor implements ASTVisitor<StaticEnvironment, CoreExpression> {
    
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
        ConstructorDefinition ctor = DataTypeDefinitions.findConstructor(constructor.name);

        return new ASTVariable(ctor.primitive).accept(this, ctx);
    }
}
