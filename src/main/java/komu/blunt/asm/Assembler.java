package komu.blunt.asm;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Assembler {

    private int labelCounter = 0;

    public Label newLabel(String prefix) {
        return new Label(checkNotNull(prefix) + "-" + labelCounter++);
    }
}
