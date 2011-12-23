package komu.blunt.parser;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;

import static java.lang.Character.*;
import static java.lang.Integer.parseInt;
import static komu.blunt.objects.Symbol.symbol;
import static komu.blunt.parser.Token.*;

public final class Lexer {
    
    private final PushbackReader reader;
    private Object nextToken = null;
    
    public Lexer(Reader reader) {
        this.reader = new PushbackReader(reader);
    }
    
    public Object peekToken() throws IOException {
        if (nextToken == null) 
            nextToken = readTokenInternal();
        return nextToken;
    }
    
    public boolean readMatchingToken(Object token) throws IOException {
        if (token.equals(peekToken())) {
            readToken();
            return true;
        } else {
            return false;
        }
    }
    
    public Object readToken() throws IOException {
        if (nextToken != null) {
            Object value = nextToken;
            nextToken = null;
            return value;
        } else {
            return readTokenInternal();
        }
    }

    public Object readTokenInternal() throws IOException {
        skipWhitespace();

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
        else if (readIf(';'))
            return readIf(';') ? DOUBLE_SEMI : SEMICOLON;
        else if (isOperatorCharacter(peek()))
            return readOperator();
        else if (isJavaIdentifierStart(peek()))
            return readIdentifierOrKeyword();
        else
            throw parseError("unexpected token: '" + read() + "'");
    }

    private void skipWhitespace() throws IOException {
        while (isWhiteSpaceOrComment(peek())) {
            char ch = read();
            if (ch == '#') {
                skipToEndOfLine();
            }
        }
    }

    private void skipToEndOfLine() throws IOException {
        while (peek() != -1)
            if (read() == '\n')
                break;
    }

    private static boolean isWhiteSpaceOrComment(int ch) {
        return ch == '#' || isWhitespace(ch);
    }

    private Object readIdentifierOrKeyword() throws IOException {
        StringBuilder sb = new StringBuilder();

        while (isIdentifierPart(peek()))
            sb.append(read());

        String name = sb.toString();
        Token token = Token.keyword(name);
        return token != null ? token : symbol(sb.toString());
    }

    private static boolean isIdentifierPart(int ch) {
        return isJavaIdentifierPart(ch) || "?!".indexOf(ch) != -1;
    }

    private Operator readOperator() throws IOException {
        StringBuilder sb = new StringBuilder();

        while (isOperatorCharacter(peek()))
            sb.append(read());

        return new Operator(sb.toString());
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
    
    private static boolean isOperatorCharacter(int ch) {
        return "=-+*/<>%?!".indexOf(ch) != -1;
    }
    
    private RuntimeException parseError(String message) {
        return new RuntimeException("value error: " + message);
    }
}
