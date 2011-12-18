package fi.evident.dojolisp.eval;

import fi.evident.dojolisp.objects.Symbol;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class VariableInfo {

    public final Symbol name;
    public final int offset;

    public VariableInfo(Symbol name, int offset) {
        if (offset < 0) throw new IllegalArgumentException("negative offset: " + offset);

        this.name = requireNonNull(name);
        this.offset = offset;
    }

    @Override
    public String toString() {
        return name.toString();
    }
}
