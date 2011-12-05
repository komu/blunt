package fi.evident.dojolisp.types;

import fi.evident.dojolisp.eval.Environment;
import fi.evident.dojolisp.eval.VariableReference;
import fi.evident.dojolisp.eval.ast.Expression;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class Lambda implements Function {
    private final VariableReference[] parameters;
    private final Expression body;
    private final Environment definitionEnv;

    public Lambda(VariableReference[] parameters, Expression body, Environment definitionEnv) {
        this.parameters = requireNonNull(parameters);
        this.body = requireNonNull(body);
        this.definitionEnv = requireNonNull(definitionEnv);
    }

    @Override
    public Object apply(Object[] args) {
        if (args.length != parameters.length)
            throw new EvaluationException("Expected " + parameters.length + " arguments, but got, " + args.length);

        Environment env = new Environment(parameters.length, definitionEnv);
        for (int i = 0, count = args.length; i < count; i++)
            env.set(parameters[i], args[i]);

        return body.evaluate(env);
    }
}
