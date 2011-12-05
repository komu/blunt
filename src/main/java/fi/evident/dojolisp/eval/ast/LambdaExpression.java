package fi.evident.dojolisp.eval.ast;

import fi.evident.dojolisp.eval.Environment;
import fi.evident.dojolisp.eval.VariableReference;
import fi.evident.dojolisp.types.Lambda;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class LambdaExpression extends Expression {

    private final VariableReference[] arguments;
    private final Expression body;

    public LambdaExpression(VariableReference[] arguments, Expression body) {
        this.arguments = arguments.clone();
        this.body = requireNonNull(body);
    }

    @Override
    public Object evaluate(Environment env) {
        return new Lambda(arguments, body, env);
    }
}
