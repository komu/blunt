package komu.blunt.parser;

import java.math.BigInteger;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Character.*;
import static komu.blunt.parser.TokenType.*;

public final class Lexer {
    
    private final SourceReader reader;
    private Token<?> nextToken = null;
    private final IndentStack indents = new IndentStack();
    private final OperatorSet operatorSet;
    
    public Lexer(String source) {
        this(source, new OperatorSet());
    }

    public Lexer(String source, OperatorSet operatorSet) {
        this.reader = new SourceReader(source);
        this.operatorSet = checkNotNull(operatorSet);
    }
    
    public TokenType<?> peekTokenType() {
        return peekToken().type;
    }

    public void expectIndentStartToken(TokenType<?> type) {
        Token token = readToken(type);
        indents.push(token.getLocation().column);
    }
    
    public void pushBlockStartAtNextToken() {
        indents.push(peekToken().getLocation().column);
    }    

    public SourceLocation getSourceLocation() {
        return reader.getLocation();
    }

    public boolean nextTokenIs(TokenType<?> type) {
        return peekTokenType() == type;
    }

    public Token<?> peekToken() {
        if (nextToken == null)
            nextToken = readTokenInternal();

        return nextToken;
    }

    public <T> Token<T> peekToken(TokenType<T> type) {
        return peekToken().asType(type);
    }
    
    public Token<?> readToken() {
        if (nextToken != null) {
            Token token = nextToken;
            nextToken = null;
            return token;
        } else {
            return readTokenInternal();
        }
    }

    public <T> Token<T> readToken(TokenType<T> type) {
        Token<?> token = readToken();
        if (token.type == type)
            return token.asType(type);
        else
            throw parseError("expected token of type " + type + ", but got " + token);
    }

    public void expectToken(TokenType<?> expected) {
        readToken(expected);
    }

    public boolean readMatchingToken(TokenType<?> type) {
        if (peekTokenType() == type) {
            readToken();
            return true;
        } else {
            return false;
        }
    }

    public Operator readOperatorMatchingLevel(int level) {
        Token<?> token = peekToken();
        if (token.type == OPERATOR) {
            Operator op = token.asType(OPERATOR).value;

            if (level == op.precedence) {
                readToken();
                return op;
            }
        }

        return null;
    }

    private Token readTokenInternal() {
        skipWhitespace();

        SourceLocation location = reader.getLocation();
        if (indents.popIf(reader.getColumn())) {
            return Token.ofType(END, location);
        }

        int ch = peek();

        switch (ch) {
        case -1:    return Token.ofType(EOF, location);
        case '"':   return readString();
        case ',':   read(); return Token.ofType(COMMA, location);
        case '(':   read(); return Token.ofType(LPAREN, location);
        case ')':   read(); return Token.ofType(RPAREN, location);
        case ';':   read(); return Token.ofType(SEMICOLON, location);
        case '[':   read(); return Token.ofType(LBRACKET, location);
        case ']':   read(); return Token.ofType(RBRACKET, location);
        }

        if (isDigit(ch))
            return readNumber();
        if (isOperatorCharacter(peek()))
            return readOperator();
        if (isIdentifierStart(peek()))
            return readIdentifierOrKeyword();

        throw parseError("unexpected token: '" + read() + "'");
    }

    private void skipWhitespace() {
        while (isWhitespace(reader.peek()) || reader.matches("--")) {
            if (reader.matches("--"))
                skipToEndOfLine();
            else
                read();
        }
    }

    private void skipToEndOfLine() {
        while (reader.peek() != -1)
            if (read() == '\n')
                break;
    }

    private Token<?> readIdentifierOrKeyword() {
        SourceLocation location = reader.getLocation();
        
        StringBuilder sb = new StringBuilder();

        while (isIdentifierPart(reader.peek()))
            sb.append(read());

        String name = sb.toString();
        
        Keyword keyword = TokenType.keyword(name);

        return (keyword != null) ? Token.ofType(keyword, location)
             : isUpperCase(name.charAt(0)) ? new Token<String>(TYPE_OR_CTOR_NAME, name, location)
             : new Token<String>(IDENTIFIER, name, location);
    }

    private static boolean isIdentifierStart(int ch) {
        return isJavaIdentifierStart(ch);
    }

    private static boolean isIdentifierPart(int ch) {
        return isJavaIdentifierPart(ch) || "?!'".indexOf(ch) != -1;
    }

    private Token readOperator() {
        SourceLocation location = reader.getLocation();

        StringBuilder sb = new StringBuilder();

        while (isOperatorCharacter(reader.peek()))
            sb.append(read());

        String op = sb.toString();

        if (op.equals("\\"))
            return Token.ofType(LAMBDA, location);
        if (op.equals("="))
            return Token.ofType(ASSIGN, location);
        if (op.equals("|"))
            return Token.ofType(OR, location);
        if (op.equals("->"))
            return Token.ofType(RIGHT_ARROW, location);
        if (op.equals("=>"))
            return Token.ofType(BIG_RIGHT_ARROW, location);
        else
            return new Token<Operator>(OPERATOR, operatorSet.operator(sb.toString()), location);
    }

    private Token readString() {
        SourceLocation location = getSourceLocation();

        StringBuilder sb = new StringBuilder();
        // TODO: escaping
        expect('"');

        while (reader.peek() != '"')
            sb.append(read());

        expect('"');

        return new Token<Object>(TokenType.LITERAL, sb.toString(), location);
    }

    private Token readNumber() {
        SourceLocation location = getSourceLocation();

        StringBuilder sb = new StringBuilder();

        while (isDigit(reader.peek()))
            sb.append(read());

        return new Token<Object>(LITERAL, new BigInteger(sb.toString()), location);
    }
    
    private char read() {
        int ch = reader.read();
        if (ch != -1)
            return (char) ch;
        else
            throw parseError("unexpected EOF");
    }

    public int peek() {
        return reader.peek();
    }

    private void expect(char expected) {
        char ch = read();
        if (ch != expected)
            throw parseError("unexpected char: " + ch);
    }
    
    private static boolean isOperatorCharacter(int ch) {
        return "=-+*/<>%?!|&$:.\\~".indexOf(ch) != -1;
    }
    
    SyntaxException parseError(String message) {
        return new SyntaxException("[" + getSourceLocation() + "] " + message);
    }

    public int save() {
        return reader.getPosition();
    }

    public void restore(int state) {
        reader.setPosition(state);
    }
}
