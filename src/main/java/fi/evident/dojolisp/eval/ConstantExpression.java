package fi.evident.dojolisp.eval;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class ConstantExpression extends Expression {

    private final Object form;

    public ConstantExpression(Object form) {
        this.form = requireNonNull(form);
    }

    @Override
    public Object evaluate(Environment env) {
        return form;
    }
}
