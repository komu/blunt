package fi.evident.dojolisp.eval;

import fi.evident.dojolisp.objects.Symbol;
import fi.evident.dojolisp.types.TypeScheme;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class StaticBinding {

    public final Symbol name;
    public final TypeScheme type;
    public final Object value;
    
    public StaticBinding(String name, TypeScheme type, Object value) {
        this(Symbol.symbol(name), type, value);
    }

    public StaticBinding(Symbol name, TypeScheme type, Object value) {
        this.name = requireNonNull(name);
        this.type = requireNonNull(type);
        this.value = value;
    }
}
