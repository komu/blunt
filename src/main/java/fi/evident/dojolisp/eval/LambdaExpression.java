package fi.evident.dojolisp.eval;

import fi.evident.dojolisp.types.Lambda;
import fi.evident.dojolisp.types.Symbol;

import java.util.List;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class LambdaExpression extends Expression {

    private final List<Symbol> argumentNames;
    private final Expression body;

    public LambdaExpression(List<Symbol> argumentNames, Expression body) {
        this.argumentNames = requireNonNull(argumentNames);
        this.body = requireNonNull(body);
    }

    @Override
    public Object evaluate(Environment env) {
        return new Lambda(argumentNames, body, env);
    }
}
