package fi.evident.dojolisp.eval;

import fi.evident.dojolisp.eval.types.Type;
import fi.evident.dojolisp.objects.Symbol;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class StaticBinding {

    public final Symbol name;
    public final Type type;
    public final Object value;
    
    public StaticBinding(String name, Type type, Object value) {
        this(Symbol.symbol(name), type, value);
    }

    public StaticBinding(Symbol name, Type type, Object value) {
        this.name = requireNonNull(name);
        this.type = requireNonNull(type);
        this.value = value;
    }
}
