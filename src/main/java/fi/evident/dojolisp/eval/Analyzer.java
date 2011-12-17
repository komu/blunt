package fi.evident.dojolisp.eval;

import fi.evident.dojolisp.ast.*;
import fi.evident.dojolisp.objects.Symbol;
import fi.evident.dojolisp.types.Kind;
import fi.evident.dojolisp.types.Type;
import fi.evident.dojolisp.types.TypeScheme;
import fi.evident.dojolisp.types.TypeVariable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static fi.evident.dojolisp.objects.Symbol.symbol;
import static fi.evident.dojolisp.utils.Objects.requireNonNull;
import static java.util.Arrays.asList;

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

    private Expression analyzeSet(List<?> form, StaticEnvironment env) {
        if (form.size() != 3) throw new SyntaxException("invalid set! form: " + form);

        VariableReference var = env.lookup((Symbol) form.get(1));
        Expression exp = analyze(form.get(2), env);
        
        return new SetExpression(var, exp); 
    }
    
    private Expression analyzeSequence(List<?> form, StaticEnvironment env) {
        if (form.isEmpty()) throw new SyntaxException("invalid sequence: " + form);

        return new SequenceExpression(analyzeAll(form, env));
    }
    
    private Expression analyzeDefine(List<?> form, StaticEnvironment env) {
        if (env != rootBindings.staticEnvironment)
            throw new AnalyzationException("only top-level defines are supported");
        
        Symbol name = (Symbol) form.get(1);

        TypeScheme scheme = Type.UNIT.quantifyAll(); // TODO: proper scheme
        VariableReference var = rootBindings.staticEnvironment.define(name, scheme);
        return new DefineExpression(name, analyze(form.get(2), env), var);
    }

    private Expression analyzeApplication(List<?> form, StaticEnvironment env) {
        Expression func = analyze(form.get(0), env);
        List<Expression> args = analyzeAll(tail(form), env);

        return new ApplicationExpression(func, args);
    }
    
    private Expression analyzeLet(List<?> form, StaticEnvironment env) {
        if (form.size() < 3) throw new SyntaxException("invalid let form: " + form);

        List<?> bindings = (List<?>) form.get(1);

        List<Object> lambda = new ArrayList<Object>();
        lambda.add(LAMBDA);
        lambda.add(bindingVariables(bindings));
        lambda.addAll(form.subList(2, form.size()));

        List<Object> call = new ArrayList<Object>();
        call.add(lambda);
        call.addAll(bindingValues(bindings));

        return analyze(call, env);
    }
    
    // (letrec ((x v) ...) body) -> (let ((x (unsafe-null)) ...) (set! x v) ... body)
    private Expression analyzeLetRec(List<?> form, StaticEnvironment env) {
        Object undefinedExpr = asList(symbol("unsafe-null"));
        List<?> bindings = (List<?>) form.get(1);
        List<?> body = form.subList(2, form.size());

        List<Object> vars = bindingVariables(bindings);
        List<Object> values = bindingValues(bindings);

        List<Object> newBindings = new ArrayList<Object>(bindings.size());
        for (Object var : vars)
            newBindings.add(asList(var, undefinedExpr));

        List<Object> let = new ArrayList<Object>();
        let.add(LET);
        let.add(newBindings);

        for (int i = 0; i < bindings.size(); i++)
            let.add(Arrays.asList(SET, vars.get(i), values.get(i)));

        let.addAll(body);
        
        return analyze(let, env);
    }

    private Expression analyzeLambda(List<?> form, StaticEnvironment env) {
        if (form.size() < 3) throw new SyntaxException("invalid lambda form: " + form);

        StaticEnvironment newEnv = new StaticEnvironment(env);

        Binding[] arguments = asParameterList(form.get(1));
    
        for (Binding argument : arguments)
            newEnv.define(argument.name, argument.type);

        Expression body = analyzeSequence(form.subList(2, form.size()), newEnv);

        return new LambdaExpression(arguments, body);
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
                    arguments[i] = new Binding(name, new TypeScheme(TypeVariable.newVar(Kind.STAR))); // TODO: is this correct?
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

    private static <T> List<T> tail(List<T> list) {
        return list.subList(1, list.size());
    }

    private static List<Object> bindingValues(List<?> bindings) {
        List<Object> values = new ArrayList<Object>(bindings.size());
        for (Object binding : bindings)
            values.add(((List<?>) binding).get(1));
        return values;
    }

    private static List<Object> bindingVariables(List<?> bindings) {
        List<Object> vars = new ArrayList<Object>(bindings.size());
        for (Object binding : bindings)
            vars.add(((List<?>) binding).get(0));
        return vars;
    }
}
