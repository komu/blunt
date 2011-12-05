package fi.evident.dojolisp.eval;

import fi.evident.dojolisp.eval.ast.*;
import fi.evident.dojolisp.types.Symbol;

import java.util.ArrayList;
import java.util.List;

import static fi.evident.dojolisp.types.Symbol.symbol;
import static java.util.Arrays.asList;

public final class Analyzer {

    private static final Symbol IF = symbol("if");
    private static final Symbol LAMBDA = symbol("lambda");
    private static final Symbol QUOTE = symbol("quote");

    public Expression analyze(Object form, StaticEnvironment env) {
        if (form instanceof Symbol)
            return analyzeVariable((Symbol) form, env);
        if (isSelfEvaluating(form))
            return new ConstantExpression(form);
        else if (form instanceof List)
            return analyzeList((List<?>) form, env);
        else
            throw new SyntaxException("unknown form: " + form);
    }

    private Expression analyzeVariable(Symbol var, StaticEnvironment env) {
        if (env.contains(var))
            return new VariableExpression(var);
        else
            throw new AnalyzationException("Unbound variable: " + var);
    }

    private Expression analyzeList(List<?> form, StaticEnvironment env) {
        if (form.isEmpty()) throw new SyntaxException("empty list can't be evaluated");

        Object head = form.get(0);
        
        if (IF.equals(head))
            return analyzeIf(form, env);
        else if (QUOTE.equals(head))
            return analyzeQuote(form);
        else if (LAMBDA.equals(head))
            return analyzeLambda(form, env);
        else
            return analyzeApplication(form, env);
    }

    private Expression analyzeApplication(List<?> form, StaticEnvironment env) {
        Expression func = analyze(form.get(0), env);
        List<Expression> args = analyzeAll(form.subList(1, form.size()), env);

        return new ApplicationExpression(func, args);
    }

    private Expression analyzeLambda(List<?> form, StaticEnvironment env) {
        if (form.size() != 3) throw new SyntaxException("invalid lambda form: " + form);

        StaticEnvironment newEnv = new StaticEnvironment(env);

        Symbol[] names = asParameterList(form.get(1));
        newEnv.defineAll(asList(names));

        return new LambdaExpression(names, analyze(form.get(2), newEnv));
    }

    private static Symbol[] asParameterList(Object form) {
        if (form instanceof List<?>) {
            List<?> list = (List<?>) form;
            Symbol[] names = new Symbol[list.size()];
            
            for (int i = 0; i < names.length; i++) {
                Object obj = list.get(i);
                if (obj instanceof Symbol) {
                    names[i] = (Symbol) obj;
                } else {
                    throw new SyntaxException("invalid argument list: " + form);
                }
            }
            
            return names;

        } else {
            throw new SyntaxException("invalid argument list: " + form);
        }
    }

    private List<Expression> analyzeAll(List<?> forms, StaticEnvironment env) {
        List<Expression> exps = new ArrayList<Expression>(forms.size());
        
        for (Object form : forms)
            exps.add(analyze(form, env));

        return exps;
    }

    private Expression analyzeQuote(List<?> form) {
        if (form.size() == 2)
            return new ConstantExpression(form.get(1));
        else
            throw new SyntaxException("invalid quote form: " + form);
    }

    private Expression analyzeIf(List<?> form, StaticEnvironment env) {
        if (form.size() == 4)
            return new IfExpression(analyze(form.get(1), env), analyze(form.get(2), env), analyze(form.get(3), env));
        else
            throw new SyntaxException("invalid if form: " + form);
    }

    private static boolean isSelfEvaluating(Object form) {
        return form instanceof Number
            || form instanceof String;
    }
}
