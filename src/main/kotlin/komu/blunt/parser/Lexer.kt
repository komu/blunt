package komu.blunt.parser

import java.math.BigInteger

import komu.blunt.parser.TokenType.*

public class Lexer(source: String, private val operatorSet: OperatorSet = OperatorSet()) {

    private val reader = SourceReader(source)
    private var nextToken: Token<Any>? = null
    private val indents = IndentStack()

    public fun hasMoreTokens(): Boolean =
        !nextTokenIs(TokenType.EOF)

    public fun peekTokenType(): TokenType<Any> =
        peekToken().tokenType

    public fun expectIndentStartToken(typ: TokenType<Any>) {
        val token = readToken(typ)
        indents.push(token.location.column)
    }

    public fun pushBlockStartAtNextToken() {
        indents.push(peekToken().location.column)
    }

    public fun nextTokenIs(typ: TokenType<Any>): Boolean =
        peekTokenType() == typ

    public fun nextTokenIsOneOf(types: Collection<TokenType<Any>>): Boolean =
        types.contains(peekTokenType())

    private fun peekToken(): Token<Any> {
        if (nextToken == null)
            nextToken = readTokenInternal()

        return nextToken!!
    }

    public fun peekTokenValue<T>(typ: TokenType<T>): T =
        peekToken().asType<T>(typ).value

    private fun readToken(): Token<Any> {
        val token = nextToken
        if (token != null) {
            nextToken = null
            return token
        } else {
            return readTokenInternal()
        }
    }

    private fun readToken<T>(typ: TokenType<T>): Token<T> {
        if (nextTokenIs(typ))
            return readToken().asType<T>(typ)
        else
            throw expectFailure("token of type $typ")
    }

    public fun readTokenValue<T>(typ: TokenType<T>): T =
        readToken(typ).value

    public fun expectToken(expected: TokenType<Any>) {
        readToken(expected)
    }

    public fun readMatchingToken(t: TokenType<Any>): Boolean {
        if (peekTokenType() == t) {
            readToken()
            return true
        } else {
            return false
        }
    }

    public fun readOperatorMatchingLevel(level: Int): Operator? {
        if (nextTokenIs(TokenType.OPERATOR)) {
            val op = peekTokenValue(TokenType.OPERATOR)

            if (level == op.precedence) {
                readToken()
                return op
            }
        }

        return null
    }

    private fun readTokenInternal(): Token<Any> {
        skipWhitespace()

        val location = reader.location
        if (indents.popIf(reader.column))
            return Token.ofType(TokenType.END, location)

        val ch = peek()
        if (ch == null)
            return Token.ofType(TokenType.EOF, location)

        when (ch) {
            '"' ->   return readString()
            ',' ->   { read(); return Token.ofType(TokenType.COMMA, location) }
            '(' ->   { read(); return Token.ofType(TokenType.LPAREN, location) }
            ')' ->   { read(); return Token.ofType(TokenType.RPAREN, location) }
            ';' ->   { read(); return Token.ofType(TokenType.SEMICOLON, location) }
            '[' ->   { read(); return Token.ofType(TokenType.LBRACKET, location) }
            ']' ->   { read(); return Token.ofType(TokenType.RBRACKET, location) }
           else ->   { }
        }

        if (isDigit(ch))
            return readNumber()
        if (isOperatorCharacter(peek()))
            return readOperator()
        if (isIdentifierStart(peek()))
            return readIdentifierOrKeyword()

        throw parseError("unexpected token: '${read()}'")
    }

    private fun skipWhitespace() {
        while (isWhitespace(reader.peek()) || reader.matches("--")) {
            if (reader.matches("--"))
                skipToEndOfLine()
            else
                read()
        }
    }

    private fun skipToEndOfLine() {
        while (reader.peek() != null)
            if (read() == '\n')
                break
    }

    private fun readIdentifierOrKeyword(): Token<Any> {
        val location = reader.location

        val sb = StringBuilder()

        while (isIdentifierPart(reader.peek()))
            sb.append(read())

        val name = sb.toString()

        if (name == "_")
            return Token.ofType(TokenType.UNDERSCORE, location)

        val keyword = TokenType.keyword(name)

        if (keyword != null)
            return Token.ofType(keyword, location)
        else if (Character.isUpperCase(name[0]))
            return Token(TokenType.TYPE_OR_CTOR_NAME, name, location)
        else
            return Token(TokenType.IDENTIFIER, name, location)
    }

    private fun isWhitespace(ch: Char?) =
        ch != null && Character.isWhitespace(ch)

    private fun isIdentifierStart(ch: Char?) =
        ch != null && Character.isJavaIdentifierStart(ch)

    private fun isIdentifierPart(ch: Char?) =
        ch != null && (Character.isJavaIdentifierPart(ch) || "?!'".lastIndexOf(ch.toChar()) != -1)

    private fun isOperatorCharacter(ch: Char?) =
        ch != null && "=-+*/<>%?!|&$:.\\~".lastIndexOf(ch) != -1

    private fun isDigit(ch: Char?) =
        ch != null && Character.isDigit(ch)

    private fun readOperator(): Token<Any> {
        val location = reader.location

        val sb = StringBuilder()

        while (isOperatorCharacter(reader.peek()))
            sb.append(read())

        val op = sb.toString()
        return when (op) {
          "\\" ->   Token.ofType(TokenType.LAMBDA, location)
          "="  ->   Token.ofType(TokenType.ASSIGN, location)
          "|"  ->   Token.ofType(TokenType.OR, location)
          "->" ->   Token.ofType(TokenType.RIGHT_ARROW, location)
          "=>" ->   Token.ofType(TokenType.BIG_RIGHT_ARROW, location)
          else ->   Token(TokenType.OPERATOR, operatorSet[op], location)
        }
    }

    private fun readString(): Token<Any> {
        val location = reader.location

        val sb = StringBuilder()
        expect('"')

        var escaped = false
        while (true) {
            val ch = read()
            if (escaped) {
                when (ch) {
                  'n'   -> sb.append('\n')
                  't'   -> sb.append('\t')
                  'r'   -> sb.append('\r')
                   else -> sb.append(ch)
                }
                escaped = false
            } else if (ch == '\\') {
                escaped = true
            } else if (ch == '"') {
                break
            } else {
                sb.append(ch)
            }
        }

        return Token(TokenType.LITERAL, sb.toString(), location)
    }

    private fun readNumber(): Token<Any> {
        val location = reader.location

        val sb = StringBuilder()

        while (isDigit(reader.peek()))
            sb.append(read())

        return Token(TokenType.LITERAL, BigInteger(sb.toString()), location)
    }

    private fun read(): Char {
        val ch = reader.read()
        if (ch != null)
            return ch
        else
            throw parseError("unexpected EOF")
    }

    private fun peek(): Char? =
        reader.peek()

    private fun expect(expected: Char) {
        val ch = read()
        if (ch != expected)
            throw parseError("unexpected char: " + ch)
    }

    public fun save(): LexerState =
        // TODO: save whole reader state, including row and column
        LexerState(reader.position, indents.toList(), nextToken)

    fun restore(state: LexerState) {
        reader.position = state.position
        indents.reset(state.indents)
        nextToken = state.nextToken
    }

    fun parseError(message: String): SyntaxException =
        SyntaxException("[${reader.location}] $message")

    fun expectFailure(expected: String): SyntaxException =
        parseError("expected $expected, but got ${readToken()}")
}
