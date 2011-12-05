package fi.evident.dojolisp.objects;

import fi.evident.dojolisp.eval.Environment;
import fi.evident.dojolisp.eval.ast.Expression;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class Lambda implements Function {
    private final int parameterCount;
    private final Expression body;
    private final Environment definitionEnv;

    public Lambda(int parameterCount, Expression body, Environment definitionEnv) {
        this.parameterCount = parameterCount;
        this.body = requireNonNull(body);
        this.definitionEnv = requireNonNull(definitionEnv);
    }

    @Override
    public Object apply(Object[] args) {
        if (args.length != parameterCount)
            throw new EvaluationException("Expected " + parameterCount + " arguments, but got, " + args.length);

        return body.evaluate(definitionEnv.extend(args));
    }
}
