package komu.blunt.parser

import java.math.BigInteger
import java.util.Collection

import java.lang.Character.*
import komu.blunt.parser.TokenType.*

public class Lexer(source: String, private val operatorSet: OperatorSet) {

    this(source: String): this(source, OperatorSet()) { }

    private val reader = SourceReader(source)
    private var nextToken: Token<in Void?>? = null
    private val indents = IndentStack()

    public fun hasMoreTokens(): Boolean =
        !nextTokenIs(EOF)

    public fun peekTokenType(): TokenType<in Void?> =
        peekToken().`type`.sure()

    public fun expectIndentStartToken<T>(typ: TokenType<T>) {
        val token = readToken(typ)
        indents.push(token.getLocation().sure().column)
    }

    public fun pushBlockStartAtNextToken() {
        indents.push(peekToken().getLocation().sure().column);
    }

    public fun nextTokenIs<T>(typ: TokenType<T>?): Boolean =
        peekTokenType() == typ

    public fun nextTokenIsOneOf(types: Collection<TokenType<out Any>?>): Boolean =
        types.contains(peekTokenType())

    private fun peekToken(): Token<in Void?> {
        if (nextToken == null)
            nextToken = readTokenInternal();

        return nextToken.sure()
    }

    public fun peekTokenValue<T>(typ: TokenType<T>?): T =
        peekToken().asType(typ).sure().value.sure()

    private fun readToken(): Token<in Void?> {
        if (nextToken != null) {
            val token = nextToken.sure();
            nextToken = null;
            return token
        } else {
            return readTokenInternal()
        }
    }

    private fun readToken<T>(typ: TokenType<T>?): Token<T> {
        if (nextTokenIs(typ))
            return readToken().asType(typ).sure()
        else
            throw expectFailure("token of type $typ")
    }

    public fun readTokenValue<T>(typ: TokenType<T>?): T {
        return readToken(typ).value;
    }

    public fun expectToken<T>(expected: TokenType<T>?) {
        readToken(expected)
    }

    public fun readMatchingToken(t: TokenType<out Any>): Boolean {
        if (peekTokenType() == t) {
            readToken();
            return true;
        } else {
            return false;
        }
    }

    public fun readOperatorMatchingLevel(level: Int): Operator? {
        if (nextTokenIs(OPERATOR)) {
            val op = peekTokenValue(OPERATOR).sure()

            if (level == op.precedence) {
                readToken();
                return op;
            }
        }

        return null;
    }
    private fun readTokenInternal(): Token<in Void?> {
        skipWhitespace();

        val location = reader.getLocation().sure()
        if (indents.popIf(reader.getColumn())) {
            return Token.ofType(END, location).sure()
        }

        val ch = peek();
        if (ch == -1)
            return Token.ofType(EOF, location).sure()

        when (ch.chr) {
            '"' ->   return readString();
            ',' ->   { read(); return Token.ofType(COMMA, location).sure() }
            '(' ->   { read(); return Token.ofType(LPAREN, location).sure() }
            ')' ->   { read(); return Token.ofType(RPAREN, location).sure() }
            ';' ->   { read(); return Token.ofType(SEMICOLON, location).sure() }
            '[' ->   { read(); return Token.ofType(LBRACKET, location).sure() }
            ']' ->   { read(); return Token.ofType(RBRACKET, location).sure() }
           else ->   { }
        }

        if (isDigit(ch))
            return readNumber();
        if (isOperatorCharacter(peek()))
            return readOperator();
        if (isIdentifierStart(peek()))
            return readIdentifierOrKeyword();

        throw parseError("unexpected token: '" + read() + "'");
    }

    private fun skipWhitespace() {
        while (isWhitespace(reader.peek()) || reader.matches("--")) {
            if (reader.matches("--"))
                skipToEndOfLine();
            else
                read();
        }
    }

    private fun skipToEndOfLine() {
        while (reader.peek() != -1)
            if (read() == '\n')
                break;
    }

    private fun readIdentifierOrKeyword(): Token<out Void?> {
        val location = reader.getLocation().sure()

        val sb = StringBuilder()

        while (isIdentifierPart(reader.peek()))
            sb.append(read());

        val name = sb.toString().sure()

        if (name == "_")
            return Token.ofType(UNDERSCORE, location).sure()

        val keyword = TokenType.keyword(name);

        if (keyword != null)
            return Token.ofType(keyword, location).sure()
        else if (isUpperCase(name[0]))
            return TYPE_OR_CTOR_NAME.make(name, location).sure()
        else
            return Token(IDENTIFIER, name, location)
    }

    private fun isIdentifierStart(ch: Int) =
        isJavaIdentifierStart(ch)

    private fun isIdentifierPart(ch: Int) =
        isJavaIdentifierPart(ch) || "?!'".lastIndexOf(ch.chr) != -1;

    private fun isOperatorCharacter(ch: Int) =
        "=-+*/<>%?!|&$:.\\~".lastIndexOf(ch.chr) != -1;

//    private Token readOperator() {
//        SourceLocation location = reader.getLocation();
//
//        StringBuilder sb = new StringBuilder();
//
//        while (isOperatorCharacter(reader.peek()))
//            sb.append(read());
//
//        String op = sb.toString();
//        switch (op) {
//        case "\\":  return Token.ofType(LAMBDA, location);
//        case "=":   return Token.ofType(ASSIGN, location);
//        case "|":   return Token.ofType(OR, location);
//        case "->":  return Token.ofType(RIGHT_ARROW, location);
//        case "=>":  return Token.ofType(BIG_RIGHT_ARROW, location);
//        default:    return new Token<>(OPERATOR, operatorSet.operator(op), location);
//        }
//    }
//
//    private Token readString() {
//        SourceLocation location = reader.getLocation();
//
//        StringBuilder sb = new StringBuilder();
//        expect('"');
//
//        boolean escaped = false;
//        while (true) {
//            char ch = read();
//            if (escaped) {
//                switch (ch) {
//                case 'n': sb.append('\n'); break;
//                case 't': sb.append('\t'); break;
//                case 'r': sb.append('\r'); break;
//                default: sb.append(ch);
//                }
//                escaped = false;
//            } else if (ch == '\\') {
//                escaped = true;
//            } else if (ch == '"') {
//                break;
//            } else {
//                sb.append(ch);
//            }
//        }
//
//        return new Token<>(TokenType.LITERAL, sb.toString(), location);
//    }
//
//    private Token readNumber() {
//        SourceLocation location = reader.getLocation();
//
//        StringBuilder sb = new StringBuilder();
//
//        while (isDigit(reader.peek()))
//            sb.append(read());
//
//        return new Token<>(LITERAL, new BigInteger(sb.toString()), location);
//    }
//
    private fun read(): Char {
        val ch = reader.read();
        if (ch != -1)
            return ch.chr
        else
            throw parseError("unexpected EOF");
    }

    private fun peek(): Int {
        return reader.peek();
    }
//
//    private void expect(char expected) {
//        char ch = read();
//        if (ch != expected)
//            throw parseError("unexpected char: " + ch);
//    }
//
//    public LexerState save() {
//        return new LexerState(reader.getPosition(), indents.toList(), nextToken);
//    }
//
//    public void restore(LexerState state) {
//        reader.setPosition(state.position);
//        indents.reset(state.indents);
//        nextToken = state.nextToken;
//    }
//
//    public SyntaxException parseError(String message) {
//        return new SyntaxException("[" + reader.getLocation() + "] " + message);
//    }
//
//    public SyntaxException expectFailure(String expected) {
//        return parseError("expected " + expected + ", but got " + readToken());
//    }
}

