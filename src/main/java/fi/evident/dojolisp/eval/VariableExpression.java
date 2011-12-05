package fi.evident.dojolisp.eval;

import fi.evident.dojolisp.types.Symbol;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class VariableExpression extends Expression {
    
    private final Symbol var;

    public VariableExpression(Symbol var) {
        this.var = requireNonNull(var);
    }

    @Override
    public Object evaluate(Environment env) {
        return env.lookup(var);
    }
}
