package komu.blunt.eval;

import static komu.blunt.utils.Objects.requireNonNull;

public final class NestedEnvironment extends Environment {

    private final Object[] bindings;
    private final Environment parent;

    public NestedEnvironment(Object[] bindings, Environment parent) {
        this.bindings = requireNonNull(bindings);
        this.parent = requireNonNull(parent);
    }

    @Override
    protected void set(int frame, int offset, Object value) {
        if (frame == 0)
            bindings[offset] = value;
        else
            parent.set(frame-1, offset, value);
    }

    @Override
    protected Object lookup(int frame, int offset) {
        if (frame == 0)
            return bindings[offset];
        else
            return parent.lookup(frame-1, offset);
    }
}
