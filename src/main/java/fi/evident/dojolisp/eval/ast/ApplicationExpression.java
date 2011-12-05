package fi.evident.dojolisp.eval.ast;

import fi.evident.dojolisp.eval.Environment;
import fi.evident.dojolisp.types.Function;

import java.util.List;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class ApplicationExpression extends Expression {
    
    private final Expression func;
    private final List<Expression> args;

    public ApplicationExpression(Expression func, List<Expression> args) {
        this.func = requireNonNull(func);
        this.args = requireNonNull(args);
    }

    @Override
    public Object evaluate(Environment env) {
        Function f = (Function) func.evaluate(env);
        Object[] parameters = evaluateArguments(env);
        
        return f.apply(parameters);
    }

    private Object[] evaluateArguments(Environment env) {
        Object[] result = new Object[args.size()];
        int i = 0;
        for (Expression arg : args)
            result[i++] = arg.evaluate(env);
        return result;
    }
}
