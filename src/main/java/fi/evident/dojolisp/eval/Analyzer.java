package fi.evident.dojolisp.eval;

import fi.evident.dojolisp.ast.*;
import fi.evident.dojolisp.objects.Symbol;
import fi.evident.dojolisp.types.Type;
import fi.evident.dojolisp.types.TypeVariable;

import java.util.ArrayList;
import java.util.List;

import static fi.evident.dojolisp.objects.Symbol.symbol;

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
        return new VariableExpression(env.lookup(var));
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

        Binding[] arguments = asParameterList(form.get(1));
    
        for (Binding argument : arguments)
            newEnv.define(argument.name, argument.type);
        
        return new LambdaExpression(arguments, analyze(form.get(2), newEnv));
    }

    private static Binding[] asParameterList(Object form) {
        if (form instanceof List<?>) {
            List<?> list = (List<?>) form;
            Binding[] arguments = new Binding[list.size()];
            
            for (int i = 0; i < arguments.length; i++) {
                Object obj = list.get(i);
                if (obj instanceof List && ((List<?>) obj).size() == 2) {
                    List<?> arg = (List<?>) obj;
                    
                    if (arg.get(0) instanceof Symbol && arg.get(1) instanceof Symbol) {
                        Symbol name = (Symbol) arg.get(0);
                        Symbol type = (Symbol) arg.get(1);
                        arguments[i] = new Binding(name, Type.forName(type.toString()));
                    } else {
                        throw new SyntaxException("invalid argument list: " + form);
                    }
                } else if (obj instanceof Symbol) {
                    Symbol name = (Symbol) obj;
                    arguments[i] = new Binding(name, new TypeVariable());
                } else {
                    throw new SyntaxException("invalid argument list: " + form);
                }
            }
            
            return arguments;

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
        if (form.size() != 4)
            throw new SyntaxException("invalid if form: " + form);

        Expression test = analyze(form.get(1), env);
        Expression consequent = analyze(form.get(2), env);
        Expression alternative = analyze(form.get(3), env);

        return new IfExpression(test, consequent, alternative);
    }

    private static boolean isSelfEvaluating(Object form) {
        return form instanceof Number
            || form instanceof String;
    }
}
