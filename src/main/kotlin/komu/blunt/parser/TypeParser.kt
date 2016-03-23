package komu.blunt.parser

import komu.blunt.parser.TokenType.Companion.BIG_RIGHT_ARROW
import komu.blunt.parser.TokenType.Companion.IDENTIFIER
import komu.blunt.parser.TokenType.Companion.LBRACKET
import komu.blunt.parser.TokenType.Companion.LPAREN
import komu.blunt.parser.TokenType.Companion.RIGHT_ARROW
import komu.blunt.parser.TokenType.Companion.RPAREN
import komu.blunt.parser.TokenType.Companion.TYPE_OR_CTOR_NAME
import komu.blunt.types.*
import java.util.*
import java.util.Collections.emptyList

class TypeParser(val lexer: Lexer) {

    constructor(s: String): this(Lexer(s)) { }

    companion object {
        private val START_TOKENS =
            listOf(LPAREN, LBRACKET, IDENTIFIER, TYPE_OR_CTOR_NAME)

        fun parseType(s: String): Type =
            TypeParser(Lexer(s)).parseType()

        fun parseScheme(s: String): Scheme =
            parseQualified(s).quantifyAll()

        fun parseQualified(s: String): Qualified<Type> =
            TypeParser(s).parseQualified()
    }

    private fun parseQualified(): Qualified<Type> {
        val predicates = parseOptionalPredicates()
        val type = parseType()
        return Qualified(predicates, type)
    }

    private fun parseOptionalPredicates(): List<Predicate> {
        if (hasPredicate()) {
            val predicates = if (lexer.peekTokenType() == LPAREN)
                lexer.inParens { lexer.commaSep { parsePredicate() } }
            else
                listOf(parsePredicate())

            lexer.expectToken(BIG_RIGHT_ARROW)

            return predicates

        } else {
            return emptyList()
        }
    }

    private fun hasPredicate(): Boolean =
        lexer.withSavedState {
            try {
                lexer.readMatchingToken(LPAREN)
                parsePredicate()
                true
            } catch (e: SyntaxException) {
                false
            }
        }

    private fun parsePredicate(): Predicate {
        val className = lexer.readTokenValue(TYPE_OR_CTOR_NAME)
        val type = parseType()
        return isIn(className, type)
    }

    fun parseType(): Type {
        var type = parseBasic()

        while (lexer.readMatchingToken(RIGHT_ARROW))
            type = Type.function(type, parseType())

        return type
    }

    private fun parseBasic(): Type =
        if (lexer.nextTokenIs(TYPE_OR_CTOR_NAME))
            parseTypeConcrete()
        else
            parseTypePrimitive()

    fun parseTypePrimitive(): Type =
        when {
            lexer.nextTokenIs(LPAREN) ->
                parseParens()
            lexer.nextTokenIs(LBRACKET) ->
                parseBrackets()
            lexer.nextTokenIs(IDENTIFIER) ->
                parseTypeVariable()
            lexer.nextTokenIs(TYPE_OR_CTOR_NAME) ->
                Type.generic(lexer.readTokenValue(TYPE_OR_CTOR_NAME))
            else ->
                lexer.expectFailure("type")
        }

    fun parseTypeConcrete(): Type {
        val name = lexer.readTokenValue(TYPE_OR_CTOR_NAME)

        val args = ArrayList<Type>()
        while (lexer.nextTokenIsOneOf(START_TOKENS))
            args.add(parseTypePrimitive())

        return Type.generic(name, args)
    }

    private fun parseParens(): Type =
        lexer.inParens {
            if (lexer.nextTokenIs(RPAREN))
                BasicType.UNIT
            else
                Type.tupleOrSingle(lexer.commaSep { parseType() })
        }

    private fun parseBrackets(): Type =
        Type.list(lexer.inBrackets { parseType() })

    fun parseTypeVariable(): Type.Var =
        Type.Var(lexer.readTokenValue(IDENTIFIER))
}

