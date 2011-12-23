package komu.blunt.eval;

import komu.blunt.objects.Symbol;

import static com.google.common.base.Preconditions.checkNotNull;

public final class VariableInfo {

    public final Symbol name;
    public final int offset;

    public VariableInfo(Symbol name, int offset) {
        if (offset < 0) throw new IllegalArgumentException("negative offset: " + offset);

        this.name = checkNotNull(name);
        this.offset = offset;
    }

    @Override
    public String toString() {
        return name.toString();
    }
}
