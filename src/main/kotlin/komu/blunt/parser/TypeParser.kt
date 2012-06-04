package komu.blunt.parser

import kotlin.util.*
import komu.blunt.types.*

import java.util.ArrayList
import java.util.Arrays
import java.util.Collections
import java.util.List

import komu.blunt.parser.TokenType.*
import komu.blunt.types.quantifyAll
import komu.blunt.types.isIn

class TypeParser(val lexer: Lexer) {

    private val START_TOKENS =
        Arrays.asList(TokenType.LPAREN, TokenType.LBRACKET, TokenType.IDENTIFIER, TokenType.TYPE_OR_CTOR_NAME).sure()

    class object {
        fun parseType(s: String): Type =
            TypeParser(Lexer(s)).parseType()

        fun parseScheme(s: String): Scheme =
            quantifyAll(parseQualified(s))

        fun parseQualified(s: String): Qualified<Type> =
            TypeParser(Lexer(s)).parseQualified()
    }

    private fun parseQualified(): Qualified<Type> {
        val predicates = parseOptionalPredicates()
        val typ = parseType()
        return Qualified(predicates, typ)
    }

    private fun parseOptionalPredicates(): List<Predicate> {
        val lexerState = lexer.save()
        try {
            val predicates = ArrayList<Predicate>()
            if (lexer.readMatchingToken(TokenType.LPAREN)) {
                do {
                    predicates.add(parsePredicate())
                } while (lexer.readMatchingToken(TokenType.COMMA))
                lexer.expectToken(TokenType.RPAREN)
            } else {
                predicates.add(parsePredicate())
            }
            lexer.expectToken(TokenType.BIG_RIGHT_ARROW)
            return predicates
        } catch (e: SyntaxException) {
            lexer.restore(lexerState)
            return Collections.emptyList<Predicate>().sure()
        }
    }

    private fun parsePredicate(): Predicate {
        val className = lexer.readTokenValue(TokenType.TYPE_OR_CTOR_NAME)
        val typ = parseType()
        return isIn(className, typ)
    }

    public fun parseType(): Type {
        var typ = parseBasic()

        while (lexer.readMatchingToken(TokenType.RIGHT_ARROW))
            typ = functionType(typ, parseType())

        return typ
    }

    private fun parseBasic(): Type =
        if (lexer.nextTokenIs(TokenType.TYPE_OR_CTOR_NAME))
            parseTypeConcrete()
        else
            parseTypePrimitive()

    public fun parseTypePrimitive(): Type =
        if (lexer.nextTokenIs(TokenType.LPAREN))
            parseParens()
        else if (lexer.nextTokenIs(TokenType.LBRACKET))
            parseBrackets()
        else if (lexer.nextTokenIs(TokenType.IDENTIFIER))
            parseTypeVariable()
        else if (lexer.nextTokenIs(TokenType.TYPE_OR_CTOR_NAME))
            genericType(lexer.readTokenValue(TokenType.TYPE_OR_CTOR_NAME))
        else
            throw lexer.expectFailure("type")

    public fun parseTypeConcrete(): Type {
        val name = lexer.readTokenValue(TokenType.TYPE_OR_CTOR_NAME)

        val args = ArrayList<Type>()
        while (lexer.nextTokenIsOneOf(START_TOKENS))
            args.add(parseTypePrimitive())

        return genericType(name, args)
    }

    private fun parseParens(): Type {
        lexer.expectToken(TokenType.LPAREN)

        if (lexer.readMatchingToken(TokenType.RPAREN))
            return BasicType.UNIT

        val types = ArrayList<Type>()
        types.add(parseType())

        while (lexer.readMatchingToken(TokenType.COMMA))
            types.add(parseType())

        lexer.expectToken(TokenType.RPAREN)

        if (types.size == 1)
            return types.first()
        else
            return tupleType(types)
    }

    private fun parseBrackets(): Type {
        lexer.expectToken(TokenType.LBRACKET)
        val elementType = parseType()
        lexer.expectToken(TokenType.RBRACKET)

        return listType(elementType)
    }

    public fun parseTypeVariable(): TypeVariable {
        val name = lexer.readTokenValue(TokenType.IDENTIFIER)
        return typeVariable(name)
    }
}

