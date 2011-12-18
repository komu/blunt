package fi.evident.dojolisp.ast;

import fi.evident.dojolisp.objects.Symbol;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class VariableBinding {
    
    public final Symbol name;
    public final Expression value;

    public VariableBinding(Symbol name, Expression value) {
        this.name = requireNonNull(name);
        this.value = requireNonNull(value);
    }
}
