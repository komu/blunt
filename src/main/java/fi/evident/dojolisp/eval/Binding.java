package fi.evident.dojolisp.eval;

import fi.evident.dojolisp.objects.Symbol;
import fi.evident.dojolisp.types.TypeScheme;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class Binding {
    public final Symbol name;
    public final TypeScheme type;

    public Binding(Symbol name, TypeScheme type) {
        this.name = requireNonNull(name);
        this.type = requireNonNull(type);
    }
}
