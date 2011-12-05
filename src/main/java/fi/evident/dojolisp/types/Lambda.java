package fi.evident.dojolisp.types;

import fi.evident.dojolisp.eval.Environment;
import fi.evident.dojolisp.eval.Expression;

import java.util.List;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class Lambda implements Function {
    private final List<Symbol> parameters;
    private final Expression body;
    private final Environment definitionEnv;

    public Lambda(List<Symbol> parameters, Expression body, Environment definitionEnv) {
        this.parameters = requireNonNull(parameters);
        this.body = requireNonNull(body);
        this.definitionEnv = requireNonNull(definitionEnv);
    }

    @Override
    public Object apply(Object[] args) {
        if (args.length != parameters.size())
            throw new EvaluationException("Expected " + parameters.size() + " arguments, but got, " + args.length);

        Environment env = new Environment(definitionEnv);
        for (int i = 0, count = args.length; i < count; i++)
            env.define(parameters.get(i), args[i]);

        return body.evaluate(env);
    }
}
