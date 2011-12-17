package fi.evident.dojolisp.eval;

import java.util.Arrays;

import static java.lang.Math.max;

public final class RootEnvironment extends Environment {

    private Object[] bindings = new Object[512];

    @Override
    protected void set(int frame, int offset, Object value) {
        checkFrame(frame);

        bindings[offset] = value;
    }

    @Override
    protected Object lookup(int frame, int offset) {
        checkFrame(frame);

        return bindings[offset];
    }

    public void define(VariableReference var, Object value) {
        checkFrame(var.frame);

        if (var.offset >= bindings.length)
            bindings = Arrays.copyOf(bindings, max(var.offset + 1, bindings.length * 2));

        bindings[var.offset] = value;
    }

    private static void checkFrame(int frame) {
        if (frame != 0) throw new IllegalArgumentException("invalid frame: " + frame);
    }
}
