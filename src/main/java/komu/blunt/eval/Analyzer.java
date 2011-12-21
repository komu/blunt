package komu.blunt.eval;

import komu.blunt.core.*;
import komu.blunt.objects.Symbol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static komu.blunt.objects.Symbol.symbol;
import static komu.blunt.utils.Objects.requireNonNull;

public final class Analyzer {

    private static final Symbol IF = symbol("if");
    private static final Symbol LAMBDA = symbol("lambda");
    private static final Symbol QUOTE = symbol("quote");
    private static final Symbol LET = symbol("let");
    private static final Symbol LETREC = symbol("letrec");
    private static final Symbol BEGIN = symbol("begin");
    private static final Symbol SET = symbol("set!");
    private static final Symbol DEFINE = symbol("define");

    private final RootBindings rootBindings;

    public Analyzer(RootBindings rootBindings) {
        this.rootBindings = requireNonNull(rootBindings);
    }

    public CoreExpression analyze(Object form, StaticEnvironment env) {
        if (form instanceof Symbol)
            return analyzeVariable((Symbol) form, env);
        if (isSelfEvaluating(form))
            return new CoreConstantExpression(form);
        else if (form instanceof List)
            return analyzeList((List<?>) form, env);
        else
            throw new SyntaxException("unknown form: " + form);
    }

    private CoreExpression analyzeVariable(Symbol var, StaticEnvironment env) {
        return new CoreVariableExpression(env.lookup(var));
    }

    private CoreExpression analyzeList(List<?> form, StaticEnvironment env) {
        if (form.isEmpty()) throw new SyntaxException("empty list can't be evaluated");

        Object head = form.get(0);

        return IF.equals(head)    ? analyzeIf(form, env)
            : QUOTE.equals(head)  ? analyzeQuote(form)
            : LAMBDA.equals(head) ? analyzeLambda(form, env)
            : LET.equals(head)    ? analyzeLet(form, env)
            : LETREC.equals(head) ? analyzeLetRec(form, env)
            : BEGIN.equals(head)  ? analyzeSequence(tail(form), env)
            : SET.equals(head)    ? analyzeSet(form, env)
            : DEFINE.equals(head) ? analyzeDefine(form, env)
            : analyzeApplication(form, env);
    }

    private CoreExpression analyzeSet(List<?> form, StaticEnvironment env) {
        if (form.size() != 3) throw new SyntaxException("invalid set! form: " + form);

        VariableReference var = env.lookup((Symbol) form.get(1));
        CoreExpression exp = analyze(form.get(2), env);
        
        return new CoreSetExpression(var, exp);
    }
    
    private CoreExpression analyzeSequence(List<?> form, StaticEnvironment env) {
        if (form.isEmpty()) throw new SyntaxException("invalid sequence: " + form);

        return new CoreSequenceExpression(analyzeAll(form, env));
    }
    
    private CoreExpression analyzeDefine(List<?> form, StaticEnvironment env) {
        if (env != rootBindings.staticEnvironment)
            throw new AnalyzationException("only top-level defines are supported");

        if (form.get(1) instanceof List<?>) {
            // (define (foo x ...) body ...) -> (define foo (lambda (x ...) body ...))
            return analyzeDefine(rewriteFunctionDefine(form), env);
        } else {
            Symbol name = (Symbol) form.get(1);

            VariableReference var = rootBindings.staticEnvironment.define(name);
            return new CoreDefineExpression(name, analyze(form.get(2), env), var, rootBindings);
        }
    }

    private static List<Object> rewriteFunctionDefine(List<?> form) {
        List<?> list = (List<?>) form.get(1);

        Symbol name = (Symbol) list.get(0);
        List<Object> lambda = new ArrayList<Object>();
        lambda.add(LAMBDA);
        lambda.add(list.subList(1, list.size()));
        lambda.addAll(form.subList(2, form.size()));

        List<Object> define = new ArrayList<Object>();
        define.add(DEFINE);
        define.add(name);
        define.add(lambda);
        return define;
    }

    private CoreExpression analyzeApplication(List<?> form, StaticEnvironment env) {
        CoreExpression func = analyze(form.get(0), env);
        List<CoreExpression> args = analyzeAll(tail(form), env);

        return new CoreApplicationExpression(func, args);
    }
    
    private CoreExpression analyzeLet(List<?> form, StaticEnvironment env) {
        if (form.size() < 3) throw new SyntaxException("invalid let form: " + form);

        List<?> bindings = (List<?>) form.get(1);

        StaticEnvironment newEnv = env.extend(bindingVariables(bindings));

        CoreExpression body = analyzeSequence(form.subList(2, form.size()), newEnv);
        return new CoreLetExpression(analyzeBindings(bindings, env), body);
    }
    
    // (letrec ((x v) ...) body) -> (let ((x (unsafe-null)) ...) (set! x v) ... body)
    private CoreExpression analyzeLetRec(List<?> form, StaticEnvironment env) {
        Object undefinedExpr = asList(symbol("unsafe-null"));
        List<?> bindings = (List<?>) form.get(1);
        List<?> body = form.subList(2, form.size());

        List<Symbol> vars = bindingVariables(bindings);
        List<Object> values = bindingValues(bindings);

        List<Object> newBindings = new ArrayList<Object>(bindings.size());
        for (Symbol var : vars)
            newBindings.add(asList(var, undefinedExpr));

        List<Object> let = new ArrayList<Object>();
        let.add(LET);
        let.add(newBindings);

        for (int i = 0; i < bindings.size(); i++)
            let.add(Arrays.asList(SET, vars.get(i), values.get(i)));

        let.addAll(body);
        
        return analyze(let, env);
    }

    private CoreExpression analyzeLambda(List<?> form, StaticEnvironment env) {
        if (form.size() < 3) throw new SyntaxException("invalid lambda form: " + form);


        List<Symbol> arguments = asParameterList(form.get(1));
        StaticEnvironment newEnv = env.extend(arguments);

        CoreExpression body = analyzeSequence(form.subList(2, form.size()), newEnv);

        return new CoreLambdaExpression(arguments, body);
    }

    private static List<Symbol> asParameterList(Object form) {
        List<?> list = (List<?>) form;
        
        List<Symbol> result = new ArrayList<Symbol>(list.size());
        for (Object object : list)
            result.add((Symbol) object);

        return result;
    }

    private List<CoreExpression> analyzeAll(List<?> forms, StaticEnvironment env) {
        List<CoreExpression> exps = new ArrayList<CoreExpression>(forms.size());
        
        for (Object form : forms)
            exps.add(analyze(form, env));

        return exps;
    }

    private CoreExpression analyzeQuote(List<?> form) {
        if (form.size() == 2)
            return new CoreConstantExpression(form.get(1));
        else
            throw new SyntaxException("invalid quote form: " + form);
    }

    private CoreExpression analyzeIf(List<?> form, StaticEnvironment env) {
        if (form.size() != 4)
            throw new SyntaxException("invalid if form: " + form);

        CoreExpression test = analyze(form.get(1), env);
        CoreExpression consequent = analyze(form.get(2), env);
        CoreExpression alternative = analyze(form.get(3), env);

        return new CoreIfExpression(test, consequent, alternative);
    }

    private static boolean isSelfEvaluating(Object form) {
        return form instanceof Number
            || form instanceof String;
    }

    private static <T> List<T> tail(List<T> list) {
        return list.subList(1, list.size());
    }

    private static List<Object> bindingValues(List<?> bindings) {
        List<Object> values = new ArrayList<Object>(bindings.size());
        for (Object binding : bindings)
            values.add(((List<?>) binding).get(1));
        return values;
    }

    private static List<Symbol> bindingVariables(List<?> bindings) {
        List<Symbol> vars = new ArrayList<Symbol>(bindings.size());
        for (Object binding : bindings)
            vars.add((Symbol) ((List<?>) binding).get(0));
        return vars;
    }

    private List<VariableBinding> analyzeBindings(List<?> bindings, StaticEnvironment env) {
        List<VariableBinding> result = new ArrayList<VariableBinding>(bindings.size());
        for (Object binding : bindings) {
            List<?> bnd = (List<?>) binding;
            Symbol name = (Symbol) bnd.get(0);
            CoreExpression value = analyze(bnd.get(1), env);
            result.add(new VariableBinding(name, value));
        }
        return result;
    }
}
