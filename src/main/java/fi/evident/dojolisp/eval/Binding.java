package fi.evident.dojolisp.eval;

import fi.evident.dojolisp.objects.Symbol;
import fi.evident.dojolisp.types.Type;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class Binding {
    public final Symbol name;
    public final Type type;

    public Binding(Symbol name, Type type) {
        this.name = requireNonNull(name);
        this.type = requireNonNull(type);
    }
}
