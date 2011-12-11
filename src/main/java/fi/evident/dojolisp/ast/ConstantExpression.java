package fi.evident.dojolisp.ast;

import fi.evident.dojolisp.eval.Environment;
import fi.evident.dojolisp.types.Type;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class ConstantExpression extends Expression {

    private final Object value;

    public ConstantExpression(Object value) {
        this.value = requireNonNull(value);
    }

    @Override
    public Object evaluate(Environment env) {
        return value;
    }

    @Override
    public Type typeCheck() {
        return (value == null) ? Type.UNIT : Type.fromClass(value.getClass());
    }
}
