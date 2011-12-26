package komu.blunt.parser;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.math.BigInteger;
import java.util.Collection;

import static com.google.common.base.Objects.equal;
import static java.lang.Character.*;
import static komu.blunt.objects.Symbol.symbol;
import static komu.blunt.parser.TokenType.*;

public final class Lexer {
    
    private final PushbackReader reader;
    private Token nextToken = null;
    
    public Lexer(Reader reader) {
        this.reader = new PushbackReader(reader);
    }
    
    public TokenType peekTokenType() throws IOException {
        return peekToken().type;
    }

    public Token peekToken() throws IOException {
        if (nextToken == null)
            nextToken = readTokenInternal();

        return nextToken;
    }

    public Token readToken() throws IOException {
        if (nextToken != null) {
            Token token = nextToken;
            nextToken = null;
            return token;
        } else {
            return readTokenInternal();
        }
    }

    public boolean readMatchingToken(TokenType type) throws IOException {
        if (peekTokenType() == type) {
            readToken();
            return true;
        } else {
            return false;
        }
    }

    public boolean readMatchingToken(TokenType type, Object value) throws IOException {
        Token token = peekToken();
        if (token.type == type && equal(token.value, value)) {
            readToken();
            return true;
        } else {
            return false;
        }
    }
    
    public <T> T readMatching(TokenType type, Collection<T> operators) throws IOException {
        Token token = peekToken();
    
        if (token.type == type) {
            @SuppressWarnings("unchecked")
            T value = (T) token.value;
            if (operators.contains(value)) {
                readToken();
                return value;
            }
        }

        return null;
    }    

    private Token readTokenInternal() throws IOException {
        skipWhitespace();

        if (peek() == -1)
            return new Token(EOF);
        else if (isDigit(peek()))
            return readNumber();
        else if (peek() == '"')
            return readString();
        else if (readIf(','))
            return new Token(COMMA);
        else if (readIf('('))
            return new Token(LPAREN);
        else if (readIf(')'))
            return new Token(RPAREN);
        else if (readIf(';'))
            return new Token(readIf(';') ? DOUBLE_SEMI : SEMICOLON);
        else if (readIf('['))
            return new Token(LBRACKET);
        else if (readIf(']'))
            return new Token(RBRACKET);
        else if (readIf('\\'))
            return new Token(TokenType.LAMBDA);
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

    private Token readIdentifierOrKeyword() throws IOException {
        StringBuilder sb = new StringBuilder();

        while (isIdentifierPart(peek()))
            sb.append(read());

        String name = sb.toString();
        TokenType type = TokenType.keyword(name);
        
        return type != null ? new Token(type) : new Token(IDENTIFIER, symbol(sb.toString()));
    }

    private static boolean isIdentifierPart(int ch) {
        return isJavaIdentifierPart(ch) || "?!".indexOf(ch) != -1;
    }

    private Token readOperator() throws IOException {
        StringBuilder sb = new StringBuilder();

        while (isOperatorCharacter(peek()))
            sb.append(read());

        String op = sb.toString();
        
        if (op.equals("="))
            return new Token(ASSIGN);
        else
            return new Token(OPERATOR, new Operator(sb.toString()));
    }

    private Token readString() throws IOException {
        StringBuilder sb = new StringBuilder();
        // TODO: escaping
        expect('"');

        while (peek() != '"')
            sb.append(read());

        expect('"');

        return new Token(TokenType.LITERAL, sb.toString());
    }

    private Token readNumber() throws IOException {
        StringBuilder sb = new StringBuilder();

        while (isDigit(peek()))
            sb.append(read());

        return new Token(LITERAL, new BigInteger(sb.toString()));
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
        return "=-+*/<>%?!|&$:.".indexOf(ch) != -1;
    }
    
    private RuntimeException parseError(String message) {
        return new RuntimeException("type error: " + message);
    }
}
