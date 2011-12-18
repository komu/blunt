package fi.evident.dojolisp.eval;

import fi.evident.dojolisp.objects.Symbol;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class VariableReference {

    /** Reference to the frame of variable, 0 being local frame */
    public final int frame;

    /** 0-based offset of variable within frame */
    public final int offset;

    public final Symbol name;
    
    public VariableReference(int frame, int offset, Symbol name) {
        if (frame < 0) throw new IllegalArgumentException("negative frame: " + frame);
        if (offset < 0) throw new IllegalArgumentException("negative offset: " + offset);

        this.frame = frame;
        this.offset = offset;
        this.name = requireNonNull(name);
    }

    @Override
    public String toString() {
        return "VariableReference [frame=" + frame + ", offset=" + offset + "]";
    }
}
