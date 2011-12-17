package fi.evident.dojolisp.eval;

public final class RootEnvironment extends Environment {

    private final Object[] bindings;

    public RootEnvironment(int size) {
        this.bindings = new Object[size];
    }

    @Override
    protected void set(int frame, int offset, Object value) {
        if (frame != 0) throw new IllegalArgumentException("invalid frame: " + frame);

        bindings[offset] = value;
    }

    @Override
    protected Object lookup(int frame, int offset) {
        if (frame != 0) throw new IllegalArgumentException("invalid frame: " + frame);

        return bindings[offset];
    }
}
