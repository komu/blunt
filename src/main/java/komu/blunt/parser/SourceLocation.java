package komu.blunt.parser;

import static com.google.common.base.Preconditions.checkArgument;

public final class SourceLocation {
    public final int line;
    public final int column;

    public SourceLocation(int line, int column) {
        checkArgument(line > 0);
        checkArgument(column > 0);
        this.line = line;
        this.column = column;
    }

    @Override
    public String toString() {
        return line + ":" + column;
    }
}
