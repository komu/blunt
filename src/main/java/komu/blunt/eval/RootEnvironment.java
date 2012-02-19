package komu.blunt.eval;

import komu.blunt.analyzer.VariableReference;

import java.util.Arrays;

import static com.google.common.base.Preconditions.checkArgument;
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
        checkArgument(frame == 0 || frame == VariableReference.GLOBAL_FRAME, "invalid frame %s", frame);
    }
}
