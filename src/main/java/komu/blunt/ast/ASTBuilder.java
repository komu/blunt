package komu.blunt.ast;

import komu.blunt.eval.SyntaxException;
import komu.blunt.objects.Symbol;

import java.util.ArrayList;
import java.util.List;

import static komu.blunt.objects.Symbol.symbol;

public final class ASTBuilder {

    private static final Symbol IF = symbol("if");
    private static final Symbol LAMBDA = symbol("lambda");
    private static final Symbol QUOTE = symbol("quote");
    private static final Symbol LET = symbol("let");
    private static final Symbol LETREC = symbol("letrec");
    private static final Symbol BEGIN = symbol("begin");
    private static final Symbol SET = symbol("set!");
    private static final Symbol DEFINE = symbol("define");

    public ASTExpression parse(Object form) {
        if (form instanceof Symbol)
            return new ASTVariable((Symbol) form);
        if (isSelfEvaluating(form))
            return new ASTConstant(form);
        else if (form instanceof List)
            return parseList((List<?>) form);
        else
            throw new SyntaxException("unknown form: " + form);
    }

    private ASTExpression parseList(List<?> form) {
        if (form.isEmpty()) throw new SyntaxException("empty list can't be evaluated");

        Object head = form.get(0);

        return IF.equals(head)    ? parseIf(form)
            : QUOTE.equals(head)  ? parseQuote(form)
            : LAMBDA.equals(head) ? parseLambda(form)
            : LET.equals(head)    ? parseLet(form)
            : LETREC.equals(head) ? parseLetRec(form)
            : BEGIN.equals(head)  ? parseSequence(tail(form))
            : SET.equals(head)    ? parseSet(form)
            : DEFINE.equals(head) ? parseDefine(form)
            : parseApplication(form);
    }

    private ASTExpression parseIf(List<?> form) {
        if (form.size() != 4)
            throw new SyntaxException("invalid if form: " + form);

        ASTExpression test = parse(form.get(1));
        ASTExpression consequent = parse(form.get(2));
        ASTExpression alternative = parse(form.get(3));

        return new ASTIf(test, consequent, alternative);
    }

    private ASTExpression parseQuote(List<?> form) {
        if (form.size() == 2)
            return new ASTConstant(form.get(1));
        else
            throw new SyntaxException("invalid quote form: " + form);
    }

    private ASTExpression parseLambda(List<?> form) {
        if (form.size() < 3) throw new SyntaxException("invalid lambda form: " + form);

        List<Symbol> arguments = asParameterList(form.get(1));
        ASTExpression body = parseSequence(form.subList(2, form.size()));

        return new ASTLambda(arguments, body);
    }

    private ASTExpression parseSet(List<?> form) {
        if (form.size() != 3) throw new SyntaxException("invalid set! form: " + form);

        Symbol var = (Symbol) form.get(1);
        ASTExpression exp = parse(form.get(2));

        return new ASTSet(var, exp);
    }

    private ASTSequence parseSequence(List<?> form) {
        if (form.isEmpty()) throw new SyntaxException("invalid sequence: " + form);

        return new ASTSequence(parseAll(form));
    }

    private ASTExpression parseDefine(List<?> form) {
        if (form.get(1) instanceof List<?>) {
            // (define (foo x ...) body ...) -> (define foo (lambda (x ...) body ...))
            List<?> list = (List<?>) form.get(1);

            Symbol name = (Symbol) list.get(0);
            List<Symbol> args = asParameterList(list.subList(1, list.size()));
            ASTExpression body = parseSequence(form.subList(2, form.size()));

            return new ASTDefine(name, new ASTLambda(args, body));
        } else {
            Symbol name = (Symbol) form.get(1);

            return new ASTDefine(name, parse(form.get(2)));
        }
    }

    private ASTExpression parseApplication(List<?> form) {
        ASTExpression func = parse(form.get(0));
        List<ASTExpression> args = parseAll(tail(form));

        return new ASTApplication(func, args);
    }

    private ASTExpression parseLet(List<?> form) {
        if (form.size() < 3) throw new SyntaxException("invalid let form: " + form);

        List<?> bindings = (List<?>) form.get(1);

        ASTExpression body = parseSequence(form.subList(2, form.size()));
        return new ASTLet(parseBindings(bindings), body);
    }

    // (letrec ((x v) ...) body) -> (let ((x (unsafe-null)) ...) (set! x v) ... body)
    private ASTExpression parseLetRec(List<?> form) {
        List<ASTBinding> bindings = parseBindings((List<?>) form.get(1));
        ASTSequence body = parseSequence(form.subList(2, form.size()));

        List<ASTBinding> newBindings = new ArrayList<ASTBinding>(bindings.size());
        List<ASTExpression> bodyExps = new ArrayList<ASTExpression>();
        for (ASTBinding binding : bindings) {
            newBindings.add(new ASTBinding(binding.name, new ASTApplication(new ASTVariable(symbol("unsafe-null")))));
            bodyExps.add(new ASTSet(binding.name, binding.expr));
        }
        
        bodyExps.addAll(body.exps);

        return new ASTLet(newBindings, new ASTSequence(bodyExps));
    }

    private static List<Symbol> asParameterList(Object form) {
        List<?> list = (List<?>) form;

        List<Symbol> result = new ArrayList<Symbol>(list.size());
        for (Object object : list)
            result.add((Symbol) object);

        return result;
    }

    private List<ASTExpression> parseAll(List<?> forms) {
        List<ASTExpression> exps = new ArrayList<ASTExpression>(forms.size());

        for (Object form : forms)
            exps.add(parse(form));

        return exps;
    }

    private static boolean isSelfEvaluating(Object form) {
        return form instanceof Number
            || form instanceof String;
    }

    private static <T> List<T> tail(List<T> list) {
        return list.subList(1, list.size());
    }

    private List<ASTBinding> parseBindings(List<?> bindings) {
        List<ASTBinding> result = new ArrayList<ASTBinding>(bindings.size());
        for (Object binding : bindings) {
            List<?> bnd = (List<?>) binding;
            result.add(new ASTBinding((Symbol) bnd.get(0), parse(bnd.get(1))));
        }
        return result;
    }
}
