package komu.blunt.parser;

import static com.google.common.base.Preconditions.checkNotNull;

final class SourceReader {

    private final String source;
    private int line = 0;
    private int column = 0;
    private int position = 0;

    public SourceReader(String source) {
        this.source = checkNotNull(source);
    }

    public int read() {
        if (position >= source.length()) return -1;

        char ch = source.charAt(position++);
        if (ch == '\n') {
            line++;
            column = 0;
        } else {
            column++;
        }
        
        return ch;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public int peek() {
        if (position < source.length())
            return source.charAt(position);
        else
            return -1;
    }
}
