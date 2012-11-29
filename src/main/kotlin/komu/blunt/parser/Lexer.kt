package komu.blunt.parser

import java.math.BigInteger
import komu.blunt.parser.TokenType.*
import komu.blunt.utils.*

public class Lexer(source: String, private val operatorSet: OperatorSet = OperatorSet()) {

    private val reader = SourceReader(source)
    private var nextToken: Token<Any>? = null
    private val indents = IndentStack()

    public fun hasMoreTokens(): Boolean =
        !nextTokenIs(EOF)

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
        val next = nextToken
        if (next != null) {
            return next
        } else {
            val next2 = readTokenInternal()
            nextToken = next2
            return next2
        }
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

    private fun readToken<T>(typ: TokenType<T>): Token<T> =
        if (nextTokenIs(typ))
            readToken().asType(typ)
        else
            throw expectFailure("token of type $typ")

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
        if (nextTokenIs(OPERATOR)) {
            val op = peekTokenValue(OPERATOR)

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
            return Token.ofType(END, location)

        val ch = peek()
        if (ch == null)
            return Token.ofType(EOF, location)

        when (ch) {
            '"' ->   return readString()
            ',' ->   { read(); return Token.ofType(COMMA, location) }
            '(' ->   { read(); return Token.ofType(LPAREN, location) }
            ')' ->   { read(); return Token.ofType(RPAREN, location) }
            ';' ->   { read(); return Token.ofType(SEMICOLON, location) }
            '[' ->   { read(); return Token.ofType(LBRACKET, location) }
            ']' ->   { read(); return Token.ofType(RBRACKET, location) }
           else ->   { }
        }

        return when {
            ch.isDigit()            -> readNumber()
            isOperatorCharacter(ch) -> readOperator()
            isIdentifierStart(ch)   -> readIdentifierOrKeyword()
            else                    -> throw parseError("unexpected token: '${read()}'")
        }
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

        val name = readWhile() { isIdentifierPart(it) }

        if (name == "_")
            return Token.ofType(UNDERSCORE, location)

        val keyword = TokenType.keyword(name)

        return when {
            keyword != null       -> Token.ofType(keyword, location)
            name[0].isUpperCase() -> Token(TYPE_OR_CTOR_NAME, name, location)
            else                  -> Token(IDENTIFIER, name, location)
        }
    }

    private fun isWhitespace(ch: Char?) =
        ch != null && ch.isWhitespace()

    private fun isIdentifierStart(ch: Char) =
        ch.isJavaIdentifierStart()

    private fun isIdentifierPart(ch: Char) =
        ch.isJavaIdentifierPart() || ch in "?!'"

    private fun isOperatorCharacter(ch: Char) =
        ch in "=-+*/<>%?!|&$:.\\~"

    private fun readOperator(): Token<Any> {
        val location = reader.location

        val op = readWhile() { isOperatorCharacter(it) }

        return when (op) {
          "\\" ->   Token.ofType(LAMBDA, location)
          "="  ->   Token.ofType(ASSIGN, location)
          "|"  ->   Token.ofType(OR, location)
          "->" ->   Token.ofType(RIGHT_ARROW, location)
          "=>" ->   Token.ofType(BIG_RIGHT_ARROW, location)
          else ->   Token(OPERATOR, operatorSet[op], location)
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

        return Token(LITERAL, sb.toString(), location)
    }

    private fun readNumber(): Token<Any> {
        val location = reader.location
        val s = readWhile() { it.isDigit() }

        return Token(LITERAL, BigInteger(s), location)
    }

    private fun readWhile(predicate: (Char) -> Boolean): String {
        val sb = StringBuilder()

        while (true) {
            val ch = reader.peek()
            if (ch == null || !predicate(ch))
                break;
            sb.append(read())
        }

        return sb.toString()
    }

    private fun read(): Char =
        reader.read() ?: throw parseError("unexpected EOF")

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
