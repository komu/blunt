package komu.blunt.parser

import komu.blunt.parser.TokenType.Companion.ASSIGN
import komu.blunt.parser.TokenType.Companion.BIG_RIGHT_ARROW
import komu.blunt.parser.TokenType.Companion.COMMA
import komu.blunt.parser.TokenType.Companion.END
import komu.blunt.parser.TokenType.Companion.EOF
import komu.blunt.parser.TokenType.Companion.IDENTIFIER
import komu.blunt.parser.TokenType.Companion.LAMBDA
import komu.blunt.parser.TokenType.Companion.LBRACKET
import komu.blunt.parser.TokenType.Companion.LITERAL
import komu.blunt.parser.TokenType.Companion.LPAREN
import komu.blunt.parser.TokenType.Companion.OPERATOR
import komu.blunt.parser.TokenType.Companion.OR
import komu.blunt.parser.TokenType.Companion.RBRACKET
import komu.blunt.parser.TokenType.Companion.RIGHT_ARROW
import komu.blunt.parser.TokenType.Companion.RPAREN
import komu.blunt.parser.TokenType.Companion.SEMICOLON
import komu.blunt.parser.TokenType.Companion.TYPE_OR_CTOR_NAME
import komu.blunt.parser.TokenType.Companion.UNDERSCORE
import java.math.BigInteger
import java.util.*

class Lexer(source: String, private val operatorSet: OperatorSet = OperatorSet()) {

    private val reader = SourceReader(source)
    private var nextToken: Token<Any>? = null
    private val indents = IndentStack()

    fun hasMoreTokens(): Boolean =
        !nextTokenIs(EOF)

    fun peekTokenType(): TokenType<Any> =
        peekToken().tokenType

    fun expectIndentStartToken(typ: TokenType<Any>) {
        val token = readToken(typ)
        indents.push(token.location.column)
    }

    fun pushBlockStartAtNextToken() {
        indents.push(peekToken().location.column)
    }

    fun nextTokenIs(typ: TokenType<Any>): Boolean =
        peekTokenType() == typ

    fun nextTokenIsOneOf(types: Collection<TokenType<Any>>): Boolean =
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

    fun <T> peekTokenValue(typ: TokenType<T>): T =
        peekToken().asType(typ).value

    private fun readToken(): Token<Any> {
        val token = nextToken
        if (token != null) {
            nextToken = null
            return token
        } else {
            return readTokenInternal()
        }
    }

    private fun <T : Any> readToken(typ: TokenType<T>): Token<T> =
        if (nextTokenIs(typ))
            readToken().asType(typ)
        else
            expectFailure("token of type $typ")

    fun <T : Any> readTokenValue(typ: TokenType<T>): T =
        readToken(typ).value

    fun expectToken(expected: TokenType<Any>) {
        readToken(expected)
    }

    fun readMatchingToken(t: TokenType<*>): Boolean {
        if (peekTokenType() == t) {
            readToken()
            return true
        } else {
            return false
        }
    }

    fun readOperatorMatchingLevel(level: Int): Operator? {
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

        val ch = peek() ?: return Token.ofType(EOF, location)

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
            else                    -> parseError("unexpected token: '${read()}'")
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

    private fun isWhitespace(ch: Char?): Boolean =
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
                break
            sb.append(read())
        }

        return sb.toString()
    }

    private fun read(): Char =
        reader.read() ?: parseError("unexpected EOF")

    private fun peek(): Char? =
        reader.peek()

    private fun expect(expected: Char) {
        val ch = read()
        if (ch != expected)
            parseError("unexpected char: " + ch)
    }

    fun save(): LexerState =
        LexerState(reader.save(), indents.toList(), nextToken)

    fun restore(state: LexerState) {
        reader.restore(state.readerState)
        indents.reset(state.indents)
        nextToken = state.nextToken
    }

    fun parseError(message: String): Nothing =
        throw SyntaxException("[${reader.location}] $message")

    fun expectFailure(expected: String): Nothing =
        parseError("expected $expected, but got ${readToken()}")

    fun <T> sepBy(separator: TokenType<*>, parser: () -> T): List<T> {
        val result = ArrayList<T>()
        do {
            result.add(parser())
        } while (readMatchingToken(separator))
        return result
    }

    fun <T> inParens(parser: () -> T): T {
        expectToken(LPAREN)
        val result = parser()
        expectToken(RPAREN)
        return result
    }

    fun <T> inBrackets(parser: () -> T): T {
        expectToken(LBRACKET)
        val result = parser()
        expectToken(RBRACKET)
        return result
    }
}
