package komu.blunt.parser

import java.math.BigInteger
import java.util.Collection

import java.lang.Character.*
import komu.blunt.parser.TokenType.*
import java.util.List

public class Lexer(source: String, private val operatorSet: OperatorSet) {

    this(source: String): this(source, OperatorSet()) { }

    private val reader = SourceReader(source)
    private var nextToken: Token<Any>? = null
    private val indents = IndentStack()

    public fun hasMoreTokens(): Boolean =
        !nextTokenIs(EOF)

    public fun peekTokenType(): TokenType<Any> =
        peekToken().`type`.sure()

    public fun expectIndentStartToken<T>(typ: TokenType<T>) {
        val token = readToken(typ)
        indents.push(token.location.column)
    }

    public fun pushBlockStartAtNextToken() {
        indents.push(peekToken().location.column);
    }

    public fun nextTokenIs<T>(typ: TokenType<T>?): Boolean =
        peekTokenType() == typ

    public fun nextTokenIsOneOf(types: Collection<TokenType<Any>>): Boolean =
        types.contains(peekTokenType())

    private fun peekToken(): Token<Any> {
        if (nextToken == null)
            nextToken = readTokenInternal();

        return nextToken.sure()
    }

    public fun peekTokenValue<T>(typ: TokenType<T>): T =
        peekToken().asType<T?>(typ.sure()).value.sure()

    private fun readToken(): Token<Any> {
        if (nextToken != null) {
            val token = nextToken.sure();
            nextToken = null;
            return token
        } else {
            return readTokenInternal()
        }
    }

    private fun readToken<T>(typ: TokenType<T>): Token<T?> {
        if (nextTokenIs(typ))
            return readToken().asType<T?>(typ.sure())
        else
            throw expectFailure("token of type $typ")
    }

    public fun readTokenValue<T>(typ: TokenType<T>): T? =
        readToken(typ).value

    public fun expectToken<T>(expected: TokenType<T>) {
        readToken(expected)
    }

    public fun readMatchingToken(t: TokenType<out Any?>?): Boolean {
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
    private fun readTokenInternal(): Token<Any> {
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

    private fun readIdentifierOrKeyword(): Token<Any> {
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
            return Token(TYPE_OR_CTOR_NAME, location)
        else
            return Token(IDENTIFIER, name, location)
    }

    private fun isIdentifierStart(ch: Int) =
        isJavaIdentifierStart(ch)

    private fun isIdentifierPart(ch: Int) =
        isJavaIdentifierPart(ch) || "?!'".lastIndexOf(ch.chr) != -1;

    private fun isOperatorCharacter(ch: Int) =
        "=-+*/<>%?!|&$:.\\~".lastIndexOf(ch.chr) != -1;

    private fun readOperator(): Token<Any> {
        val location = reader.getLocation()

        val sb = StringBuilder()

        while (isOperatorCharacter(reader.peek()))
            sb.append(read())

        val op = sb.toString().sure()
        return when (op) {
          "\\" ->   Token.ofType(LAMBDA, location)
          "="  ->   Token.ofType(ASSIGN, location)
          "|"  ->   Token.ofType(OR, location)
          "->" ->   Token.ofType(RIGHT_ARROW, location)
          "=>"  ->  Token.ofType(BIG_RIGHT_ARROW, location)
          else ->   Token(OPERATOR, operatorSet.operator(op), location.sure())
        }.sure()
    }

    private fun readString(): Token<Any> {
        val location = reader.getLocation()

        val sb = StringBuilder()
        expect('"')

        var escaped = false;
        while (true) {
            val ch = read()
            if (escaped) {
                when (ch) {
                  'n'   -> sb.append('\n');
                  't'   -> sb.append('\t');
                  'r'   -> sb.append('\r');
                   else -> sb.append(ch);
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

        return Token(TokenType.LITERAL, sb.toString(), location.sure())
    }

    private fun readNumber(): Token<Any> {
        val location = reader.getLocation().sure()

        val sb = StringBuilder()

        while (isDigit(reader.peek()))
            sb.append(read())

        return Token(LITERAL, BigInteger(sb.toString()), location)
    }

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

    private fun expect(expected: Char) {
        val ch = read()
        if (ch != expected)
            throw parseError("unexpected char: " + ch)
    }

    public fun save(): LexerState =
        LexerState(reader.getPosition(), indents.toList(), nextToken)

    fun restore(state: LexerState) {
        reader.setPosition(state.position)
        indents.reset(state.indents)
        nextToken = state.nextToken
    }

    fun parseError(message: String): SyntaxException =
        SyntaxException("[${reader.getLocation()}] $message");

    fun expectFailure(expected: String): SyntaxException =
        parseError("expected $expected, but got ${readToken()}")
}
