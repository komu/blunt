package fi.evident.dojolisp.eval;

import fi.evident.dojolisp.objects.Symbol;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class StaticBinding {

    public final Symbol name;
    public final Object value;
    
    public StaticBinding(String name, Object value) {
        this(Symbol.symbol(name), value);
    }

    public StaticBinding(Symbol name, Object value) {
        this.name = requireNonNull(name);
        this.value = value;
    }
}
