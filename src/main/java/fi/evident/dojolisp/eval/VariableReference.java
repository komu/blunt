package fi.evident.dojolisp.eval;

import fi.evident.dojolisp.types.Type;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class VariableReference {

    /** Reference to the frame of variable, 0 being local frame */
    public final int frame;

    /** 0-based offset of variable within frame */
    public final int offset;
    
    public final Type type;
    
    public VariableReference(int frame, int offset, Type type) {
        if (frame < 0) throw new IllegalArgumentException("negative frame: " + frame);
        if (offset < 0) throw new IllegalArgumentException("negative offset: " + offset);

        this.frame = frame;
        this.offset = offset;
        this.type = requireNonNull(type);
    }

    @Override
    public String toString() {
        return "VariableReference [frame=" + frame + ", offset=" + offset + "]";
    }
}
