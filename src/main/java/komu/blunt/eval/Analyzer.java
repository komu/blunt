package komu.blunt.eval;

import komu.blunt.ast.*;
import komu.blunt.core.*;

import java.util.ArrayList;
import java.util.List;

import static komu.blunt.utils.Objects.requireNonNull;

public final class Analyzer {

    private final RootBindings rootBindings;

    public Analyzer(RootBindings rootBindings) {
        this.rootBindings = requireNonNull(rootBindings);
    }
    
    public CoreExpression analyze(Object form, StaticEnvironment env) {
        return analyzeInternal(new ASTBuilder().parse(form), env);
    }
    
    private CoreExpression analyzeInternal(ASTExpression exp, StaticEnvironment env) {
        if (exp instanceof ASTVariable)
            return new CoreVariableExpression(env.lookup(((ASTVariable) exp).var));
        else if (exp instanceof ASTConstant)
            return new CoreConstantExpression(((ASTConstant) exp).value);
        else if (exp instanceof ASTIf)
            return analyzeIf((ASTIf) exp, env);
        else if (exp instanceof ASTLambda)
            return analyzeLambda((ASTLambda) exp, env);
        else if (exp instanceof ASTLet)
            return analyzeLet((ASTLet) exp, env);
        else if (exp instanceof ASTSequence)
            return analyzeSequence((ASTSequence) exp, env);
        else if (exp instanceof ASTSet)
            return analyzeSet((ASTSet) exp, env);
        else if (exp instanceof ASTDefine)
            return analyzeDefine((ASTDefine) exp, env);
        else if (exp instanceof ASTApplication)
            return analyzeApplication((ASTApplication) exp, env);
        else
            throw new SyntaxException("unknown exp: " + exp);
    }

    private CoreExpression analyzeSet(ASTSet form, StaticEnvironment env) {
        VariableReference var = env.lookup(form.var);
        CoreExpression exp = analyzeInternal(form.exp, env);
        
        return new CoreSetExpression(var, exp);
    }
    
    private CoreExpression analyzeSequence(ASTSequence form, StaticEnvironment env) {
        if (form.exps.isEmpty()) throw new SyntaxException("invalid sequence: " + form);

        return new CoreSequenceExpression(analyzeAll(form.exps, env));
    }
    
    private CoreExpression analyzeDefine(ASTDefine form, StaticEnvironment env) {
        VariableReference var = rootBindings.staticEnvironment.define(form.name);
        return new CoreDefineExpression(form.name, analyzeInternal(form.value, env), var, rootBindings);
    }

    private CoreExpression analyzeApplication(ASTApplication form, StaticEnvironment env) {
        CoreExpression func = analyzeInternal(form.func, env);
        List<CoreExpression> args = analyzeAll(form.args, env);

        return new CoreApplicationExpression(func, args);
    }
    
    private CoreExpression analyzeLet(ASTLet form, StaticEnvironment env) {
        StaticEnvironment newEnv = env.extend(form.getVariables());

        CoreExpression body = analyzeInternal(form.body, newEnv);
        return new CoreLetExpression(analyzeBindings(form.bindings, env), body);
    }

    private CoreExpression analyzeLambda(ASTLambda form, StaticEnvironment env) {
        StaticEnvironment newEnv = env.extend(form.arguments);

        CoreExpression body = analyzeInternal(form.body, newEnv);

        return new CoreLambdaExpression(form.arguments, body);
    }

    private List<CoreExpression> analyzeAll(List<ASTExpression> forms, StaticEnvironment env) {
        List<CoreExpression> exps = new ArrayList<CoreExpression>(forms.size());
        
        for (ASTExpression form : forms)
            exps.add(analyzeInternal(form, env));

        return exps;
    }

    private CoreExpression analyzeIf(ASTIf form, StaticEnvironment env) {
        CoreExpression test = analyzeInternal(form.test, env);
        CoreExpression consequent = analyzeInternal(form.consequent, env);
        CoreExpression alternative = analyzeInternal(form.alternative, env);

        return new CoreIfExpression(test, consequent, alternative);
    }

    private List<VariableBinding> analyzeBindings(List<ASTBinding> bindings, StaticEnvironment env) {
        List<VariableBinding> result = new ArrayList<VariableBinding>(bindings.size());
        for (ASTBinding binding : bindings)
            result.add(new VariableBinding(binding.name, analyzeInternal(binding.expr, env)));
        return result;
    }
}
