package komu.blunt.parser;

import java.io.IOException;
import java.io.Reader;

import static com.google.common.base.Preconditions.checkNotNull;

final class SourceReader {

    private final Reader reader;
    private int peeked = -1;
    private int line = 0;
    private int column = 0;

    public SourceReader(Reader reader) {
        this.reader = checkNotNull(reader);
    }

    public int read() throws IOException {
        int ch = readInternal();
        if (ch == '\n') {
            line++;
            column = 0;
        } else if (ch != -1) {
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

    private int readInternal() throws IOException {
        if (peeked != -1) {
            int value = peeked;
            peeked = -1;
            return value;
        }
        return reader.read();
    }

    public int peek() throws IOException {
        if (peeked == -1) {
            peeked = reader.read();
        }
        return peeked;
    }
}
