package fi.evident.dojolisp.eval.ast;

import fi.evident.dojolisp.eval.Environment;
import fi.evident.dojolisp.types.Lambda;
import fi.evident.dojolisp.types.Symbol;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class LambdaExpression extends Expression {

    private final Symbol[] argumentNames;
    private final Expression body;

    public LambdaExpression(Symbol[] argumentNames, Expression body) {
        this.argumentNames = argumentNames.clone();
        this.body = requireNonNull(body);
    }

    @Override
    public Object evaluate(Environment env) {
        return new Lambda(argumentNames, body, env);
    }
}
