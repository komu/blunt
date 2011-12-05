package fi.evident.dojolisp.eval.ast;

import fi.evident.dojolisp.eval.Environment;
import fi.evident.dojolisp.objects.Lambda;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class LambdaExpression extends Expression {

    private final int parameterCount;
    private final Expression body;

    public LambdaExpression(int parameterCount, Expression body) {
        if (parameterCount < 0) throw new IllegalArgumentException("negative parameterCount");

        this.parameterCount = parameterCount;
        this.body = requireNonNull(body);
    }

    @Override
    public Object evaluate(Environment env) {
        return new Lambda(parameterCount, body, env);
    }
}
