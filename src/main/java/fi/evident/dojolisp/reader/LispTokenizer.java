package fi.evident.dojolisp.reader;

import fi.evident.dojolisp.objects.Symbol;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;

import static fi.evident.dojolisp.objects.Symbol.symbol;
import static fi.evident.dojolisp.reader.Token.*;
import static java.lang.Character.*;
import static java.lang.Integer.parseInt;

public final class LispTokenizer {
    
    private final PushbackReader reader;
    
    public LispTokenizer(Reader reader) {
        this.reader = new PushbackReader(reader);
    }
    
    public Object readToken() throws IOException {
        while (isWhitespace(peek()))
            read();

        if (peek() == -1)
            return EOF;
        else if (isDigit(peek()))
            return readNumber();
        else if (peek() == '"')
            return readString();
        else if (readIf('('))
            return LPAREN;
        else if (readIf(')'))
            return RPAREN;
        else if (readIf('\''))
            return QUOTE;
        else if (isSymbolCharacter(peek()))
            return readSymbol();
        else
            throw parseError("unexpected token: '" + read() + "'");
    }
    private Symbol readSymbol() throws IOException {
        StringBuilder sb = new StringBuilder();

        while (isSymbolCharacter(peek()))
            sb.append(read());
        
        return symbol(sb.toString());
    }

    private String readString() throws IOException {
        StringBuilder sb = new StringBuilder();
        // TODO: escaping
        expect('"');

        while (peek() != '"')
            sb.append(read());

        expect('"');

        return sb.toString();
    }

    private Number readNumber() throws IOException {
        StringBuilder sb = new StringBuilder();

        while (isDigit(peek()))
            sb.append(read());

        return parseInt(sb.toString());
    }
    
    private char read() throws IOException {
        int ch = reader.read();
        if (ch != -1)
            return (char) ch;
        else
            throw parseError("unexpected EOF");
    }

    private boolean readIf(char expected) throws IOException {
        int ch = reader.read();
        if (ch == expected) {
            return true;
        } else {
            if (ch != -1)
                reader.unread(ch);
            return false;
        }
    }

    private int peek() throws IOException {
        int ch = reader.read();
        if (ch != -1)
            reader.unread(ch);
        return ch;        
    }
    
    private void expect(char expected) throws IOException {
        char ch = read();
        if (ch != expected)
            throw parseError("unexpected char: " + ch);
    }
    
    private static boolean isSymbolCharacter(int ch) {
        return isLetterOrDigit(ch) || "=-_+*/<>%?!".indexOf(ch) != -1;
    }
    
    private RuntimeException parseError(String message) {
        return new RuntimeException("parse error: " + message);
    }
}
