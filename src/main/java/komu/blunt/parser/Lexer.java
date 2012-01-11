package komu.blunt.parser;

import java.math.BigInteger;
import java.util.Collection;

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

    public boolean hasMoreTokens() {
        return !nextTokenIs(EOF);
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

    public boolean nextTokenIs(TokenType<?> type) {
        return peekTokenType() == type;
    }

    public boolean nextTokenIsOneOf(Collection<TokenType<?>> types) {
        return types.contains(peekTokenType());
    }

    private Token<?> peekToken() {
        if (nextToken == null)
            nextToken = readTokenInternal();

        return nextToken;
    }

    public <T> T peekTokenValue(TokenType<T> type) {
        return peekToken().asType(type).value;
    }
    
    private Token<?> readToken() {
        if (nextToken != null) {
            Token token = nextToken;
            nextToken = null;
            return token;
        } else {
            return readTokenInternal();
        }
    }

    private <T> Token<T> readToken(TokenType<T> type) {
        if (nextTokenIs(type))
            return readToken().asType(type);
        else
            throw expectFailure("token of type " + type);
    }

    public <T> T readTokenValue(TokenType<T> type) {
        return readToken(type).value;
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
        if (nextTokenIs(OPERATOR)) {
            Operator op = peekTokenValue(OPERATOR);

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
        
        if (name.equals("_"))
            return Token.ofType(UNDERSCORE, location);

        Keyword keyword = TokenType.keyword(name);

        return (keyword != null) ? Token.ofType(keyword, location)
             : isUpperCase(name.charAt(0)) ? new Token<>(TYPE_OR_CTOR_NAME, name, location)
             : new Token<>(IDENTIFIER, name, location);
    }

    private static boolean isIdentifierStart(int ch) {
        return isJavaIdentifierStart(ch);
    }

    private static boolean isIdentifierPart(int ch) {
        return isJavaIdentifierPart(ch) || "?!'".indexOf(ch) != -1;
    }

    private static boolean isOperatorCharacter(int ch) {
        return "=-+*/<>%?!|&$:.\\~".indexOf(ch) != -1;
    }

    private Token readOperator() {
        SourceLocation location = reader.getLocation();

        StringBuilder sb = new StringBuilder();

        while (isOperatorCharacter(reader.peek()))
            sb.append(read());

        String op = sb.toString();
        switch (op) {
        case "\\":  return Token.ofType(LAMBDA, location);
        case "=":   return Token.ofType(ASSIGN, location);
        case "|":   return Token.ofType(OR, location);
        case "->":  return Token.ofType(RIGHT_ARROW, location);
        case "=>":  return Token.ofType(BIG_RIGHT_ARROW, location);
        default:    return new Token<>(OPERATOR, operatorSet.operator(op), location);
        }
    }

    private Token readString() {
        SourceLocation location = reader.getLocation();

        StringBuilder sb = new StringBuilder();
        expect('"');

        boolean escaped = false;
        while (true) {
            char ch = read();
            if (escaped) {
                switch (ch) {
                case 'n': sb.append('\n'); break;
                case 't': sb.append('\t'); break;
                case 'r': sb.append('\r'); break;
                default: sb.append(ch);
                }
                escaped = false;
            } else if (ch == '\\') {
                escaped = true;
            } else if (ch == '"') {
                break;
            } else {
                sb.append(ch);
            }
        }

        return new Token<>(TokenType.LITERAL, sb.toString(), location);
    }

    private Token readNumber() {
        SourceLocation location = reader.getLocation();

        StringBuilder sb = new StringBuilder();

        while (isDigit(reader.peek()))
            sb.append(read());

        return new Token<>(LITERAL, new BigInteger(sb.toString()), location);
    }
    
    private char read() {
        int ch = reader.read();
        if (ch != -1)
            return (char) ch;
        else
            throw parseError("unexpected EOF");
    }

    private int peek() {
        return reader.peek();
    }

    private void expect(char expected) {
        char ch = read();
        if (ch != expected)
            throw parseError("unexpected char: " + ch);
    }

    public LexerState save() {
        return new LexerState(reader.getPosition(), indents.toList(), nextToken);
    }

    public void restore(LexerState state) {
        reader.setPosition(state.position);
        indents.reset(state.indents);
        nextToken = state.nextToken;
    }

    public SyntaxException parseError(String message) {
        return new SyntaxException("[" + reader.getLocation() + "] " + message);
    }

    public SyntaxException expectFailure(String expected) {
        return parseError("expected " + expected + ", but got " + readToken());
    }
}
