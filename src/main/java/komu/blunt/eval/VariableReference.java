package komu.blunt.eval;

import komu.blunt.objects.Symbol;

import static com.google.common.base.Preconditions.checkNotNull;

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
        this.name = checkNotNull(name);
    }

    @Override
    public String toString() {
        return "VariableReference [frame=" + frame + ", offset=" + offset + "]";
    }
}
