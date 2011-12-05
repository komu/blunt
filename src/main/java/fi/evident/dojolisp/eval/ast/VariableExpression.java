package fi.evident.dojolisp.eval.ast;

import fi.evident.dojolisp.eval.Environment;
import fi.evident.dojolisp.eval.VariableReference;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class VariableExpression extends Expression {
    
    private final VariableReference var;

    public VariableExpression(VariableReference var) {
        this.var = requireNonNull(var);
    }

    @Override
    public Object evaluate(Environment env) {
        return env.lookup(var);
    }
}
