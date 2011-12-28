package komu.blunt.parser;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

final class SourceReader {

    private final Reader reader;
    private int peeked = -1;
    private int line = 0;
    private int column = 0;

    public SourceReader(String source) {
        this.reader = new StringReader(source);
    }

    public int read() {
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

    private int readInternal() {
        if (peeked != -1) {
            int value = peeked;
            peeked = -1;
            return value;
        }
        return readCharFromBuffer();
    }

    public int peek() {
        if (peeked == -1) {
            peeked = readCharFromBuffer();
        }
        return peeked;
    }
    
    private int readCharFromBuffer() {
        try {
            return reader.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
