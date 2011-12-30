package komu.blunt.eval;

import java.util.Arrays;

import static com.google.common.base.Preconditions.checkNotNull;

public final class NestedEnvironment extends Environment {

    private Object[] bindings;
    private final Environment parent;

    public NestedEnvironment(Object[] bindings, Environment parent) {
        this.bindings = bindings;
        this.parent = checkNotNull(parent);
    }

    @Override
    protected void set(int frame, int offset, Object value) {
        ensureSize(offset);
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

    private void ensureSize(int offset) {
        // TODO: create environments of correct size in the first place
        if (offset >= bindings.length) {
            bindings = Arrays.copyOf(bindings, offset*2);
        }
    }
}
