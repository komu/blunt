package komu.blunt.parser;

import static com.google.common.base.Preconditions.checkNotNull;

final class SourceReader {

    private final String source;
    private int line = 1;
    private int column = 1;
    private int position = 0;

    public SourceReader(String source) {
        this.source = checkNotNull(source);
    }

    public int read() {
        if (!hasMore()) return -1;

        char ch = source.charAt(position++);
        if (ch == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }
        
        return ch;
    }
    
    public boolean matches(String s) {
        assert !s.isEmpty();
        
        if (!hasMore()) return false;
        
        return source.regionMatches(position, s, 0, s.length());
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public int peek() {
        if (hasMore())
            return source.charAt(position);
        else
            return -1;
    }

    private boolean hasMore() {
        return position < source.length();
    }

    public SourceLocation getLocation() {
        return new SourceLocation(line, column);
    }
}
