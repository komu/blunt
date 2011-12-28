package komu.blunt.parser;

import java.io.IOException;
import java.io.Reader;
import java.math.BigInteger;
import java.util.Collection;

import static com.google.common.base.Objects.equal;
import static java.lang.Character.*;
import static komu.blunt.parser.TokenType.*;

public final class Lexer {
    
    private final SourceReader reader;
    private Token<?> nextToken = null;
    private final IndentStack indents = new IndentStack();
    
    public Lexer(Reader reader) {
        this.reader = new SourceReader(reader);
    }
    
    public TokenType<?> peekTokenType() throws IOException {
        return peekToken().type;
    }

    public void pushIndentLevelAtNextToken() throws IOException {
        skipWhitespace();
        
        indents.push(reader.getColumn());
    }

    public String getSourceLocation() {
        return reader.getLine() + ":" + reader.getColumn();
    }

    public boolean nextTokenIs(TokenType<?> type) throws IOException {
        return peekTokenType() == type;
    }

    public Token<?> peekToken() throws IOException {
        if (nextToken == null)
            nextToken = readTokenInternal();

        return nextToken;
    }

    public Token<?> readToken() throws IOException {
        if (nextToken != null) {
            Token token = nextToken;
            nextToken = null;
            return token;
        } else {
            return readTokenInternal();
        }
    }

    public <T> Token<T> readToken(TokenType<T> type) throws IOException {
        Token<?> token = readToken();
        if (token.type == type)
            return token.asType(type);
        else
            throw parseError("expected token of type " + type + ", but got " + token);
    }
    
    public boolean readMatchingToken(TokenType<?> type) throws IOException {
        if (peekTokenType() == type) {
            readToken();
            return true;
        } else {
            return false;
        }
    }

    public <T> boolean readMatchingToken(TokenType<T> type, T value) throws IOException {
        Token<?> token = peekToken();
        if (token.type == type && equal(token.value, value)) {
            readToken();
            return true;
        } else {
            return false;
        }
    }
    
    public <T> T readMatching(TokenType<T> type, Collection<T> operators) throws IOException {
        Token<?> token = peekToken();

        if (token.type == type) {
            T value = token.asType(type).value;
            if (operators.contains(value)) {
                readToken();
                return value;
            }
        }

        return null;
    }    

    private Token readTokenInternal() throws IOException {
        skipWhitespace();

        if (indents.popIf(reader.getColumn()))
            return Token.ofType(END);

        int ch = peek();

        switch (ch) {
        case -1:    return Token.ofType(EOF);
        case '"':   return readString();
        case ',':   read(); return Token.ofType(COMMA);
        case '(':   read(); return Token.ofType(LPAREN);
        case ')':   read(); return Token.ofType(RPAREN);
        case ';':   read(); return Token.ofType(SEMICOLON);
        case '[':   read(); return Token.ofType(LBRACKET);
        case ']':   read(); return Token.ofType(RBRACKET);
        }

        if (isDigit(ch))
            return readNumber();
        if (isOperatorCharacter(peek()))
            return readOperator();
        if (isIdentifierStart(peek()))
            return readIdentifierOrKeyword();

        throw parseError("unexpected token: '" + read() + "'");
    }

    private void skipWhitespace() throws IOException {
        while (isWhiteSpaceOrComment(reader.peek())) {
            char ch = read();
            if (ch == '#') {
                skipToEndOfLine();
            }
        }
    }

    private void skipToEndOfLine() throws IOException {
        while (reader.peek() != -1)
            if (read() == '\n')
                break;
    }

    private static boolean isWhiteSpaceOrComment(int ch) {
        return ch == '#' || isWhitespace(ch);
    }

    private Token<?> readIdentifierOrKeyword() throws IOException {
        StringBuilder sb = new StringBuilder();

        while (isIdentifierPart(reader.peek()))
            sb.append(read());

        String name = sb.toString();
        
        Keyword keyword = TokenType.keyword(name);

        return (keyword != null) ? Token.ofType(keyword)
             : isUpperCase(name.charAt(0)) ? new Token<String>(CONSTRUCTOR_NAME, name)
             : new Token<String>(IDENTIFIER, name);
    }

    private static boolean isIdentifierStart(int ch) {
        return isJavaIdentifierStart(ch);
    }

    private static boolean isIdentifierPart(int ch) {
        return isJavaIdentifierPart(ch) || "?!".indexOf(ch) != -1;
    }

    private Token readOperator() throws IOException {
        StringBuilder sb = new StringBuilder();

        while (isOperatorCharacter(reader.peek()))
            sb.append(read());

        String op = sb.toString();

        if (op.equals("\\"))
            return Token.ofType(LAMBDA);
        if (op.equals("="))
            return Token.ofType(ASSIGN);
        else
            return new Token<Operator>(OPERATOR, new Operator(sb.toString()));
    }

    private Token readString() throws IOException {
        StringBuilder sb = new StringBuilder();
        // TODO: escaping
        expect('"');

        while (reader.peek() != '"')
            sb.append(read());

        expect('"');

        return new Token<Object>(TokenType.LITERAL, sb.toString());
    }

    private Token readNumber() throws IOException {
        StringBuilder sb = new StringBuilder();

        while (isDigit(reader.peek()))
            sb.append(read());

        return new Token<Object>(LITERAL, new BigInteger(sb.toString()));
    }
    
    private char read() throws IOException {
        int ch = reader.read();
        if (ch != -1)
            return (char) ch;
        else
            throw parseError("unexpected EOF");
    }

    public int peek() throws IOException {
        return reader.peek();
    }

    private void expect(char expected) throws IOException {
        char ch = read();
        if (ch != expected)
            throw parseError("unexpected char: " + ch);
    }
    
    private static boolean isOperatorCharacter(int ch) {
        return "=-+*/<>%?!|&$:.\\".indexOf(ch) != -1;
    }
    
    SyntaxException parseError(String message) {
        return new SyntaxException("[" + getSourceLocation() + "] " + message);
    }
}
