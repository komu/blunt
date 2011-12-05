package fi.evident.dojolisp.types;

import fi.evident.dojolisp.eval.Environment;
import fi.evident.dojolisp.eval.Expression;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class Lambda implements Function {
    private final Symbol[] parameters;
    private final Expression body;
    private final Environment definitionEnv;

    public Lambda(Symbol[] parameters, Expression body, Environment definitionEnv) {
        this.parameters = requireNonNull(parameters);
        this.body = requireNonNull(body);
        this.definitionEnv = requireNonNull(definitionEnv);
    }

    @Override
    public Object apply(Object[] args) {
        if (args.length != parameters.length)
            throw new EvaluationException("Expected " + parameters.length + " arguments, but got, " + args.length);

        Environment env = new Environment(definitionEnv);
        for (int i = 0, count = args.length; i < count; i++)
            env.define(parameters[i], args[i]);

        return body.evaluate(env);
    }
}
