package fi.evident.dojolisp.eval;

import fi.evident.dojolisp.objects.Symbol;
import fi.evident.dojolisp.types.Type;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class VariableInfo {

    public final Symbol name;
    public final int offset;
    public final Type type;

    public VariableInfo(Symbol name, int offset, Type type) {
        if (offset < 0) throw new IllegalArgumentException("negative offset: " + offset);

        this.name = requireNonNull(name);
        this.offset = offset;
        this.type = requireNonNull(type);
    }

    @Override
    public String toString() {
        return name + ": " + type;
    }
}
