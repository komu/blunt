package komu.blunt.analyzer;

import komu.blunt.objects.Symbol;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class VariableReference {

    public static final int GLOBAL_FRAME = -1;

    /** Reference to the frame of variable, 0 being local frame, or -1 being global variable */
    public final int frame;

    /** 0-based offset of variable within frame */
    public final int offset;

    public final Symbol name;

    private VariableReference(int frame, int offset, Symbol name) {
        checkArgument(frame >= 0 || frame == GLOBAL_FRAME);
        checkArgument(offset >= 0);

        this.frame = frame;
        this.offset = offset;
        this.name = checkNotNull(name);
    }

    public boolean isGlobal() {
        return frame == GLOBAL_FRAME;
    }

    public static VariableReference nested(int frame, int offset, Symbol name) {
        return new VariableReference(frame, offset, name);
    }

    public static VariableReference global(int offset, Symbol name) {
        return new VariableReference(GLOBAL_FRAME, offset, name);
    }

    @Override
    public String toString() {
        return "VariableReference [frame=" + frame + ", offset=" + offset + "]";
    }
}
