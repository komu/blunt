package komu.blunt.parser

import komu.blunt.types.*

import java.util.ArrayList
import java.util.Arrays
import java.util.Collections
import java.util.List

import komu.blunt.parser.TokenType.*
import komu.blunt.types.Type.*
import komu.blunt.types.quantifyAll
import komu.blunt.types.isIn

class TypeParser(val lexer: Lexer) {

    private val START_TOKENS =
        Arrays.asList(LPAREN, LBRACKET, IDENTIFIER, TYPE_OR_CTOR_NAME).sure()

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

    private fun parseOptionalPredicates(): List<Predicate?> {
        val lexerState = lexer.save()
        try {
            val predicates = ArrayList<Predicate?>()
            if (lexer.readMatchingToken(LPAREN)) {
                do {
                    predicates.add(parsePredicate())
                } while (lexer.readMatchingToken(COMMA))
                lexer.expectToken(RPAREN)
            } else {
                predicates.add(parsePredicate())
            }
            lexer.expectToken(TokenType.BIG_RIGHT_ARROW)
            return predicates
        } catch (e: SyntaxException) {
            lexer.restore(lexerState)
            return Collections.emptyList<Predicate?>().sure()
        }
    }

    private fun parsePredicate(): Predicate {
        val className = lexer.readTokenValue(TYPE_OR_CTOR_NAME)
        val typ = parseType()
        return isIn(className, typ)
    }

    public fun parseType(): Type {
        var typ = parseBasic()

        while (lexer.readMatchingToken(RIGHT_ARROW))
            typ = functionType(typ, parseType()).sure()

        return typ
    }

    private fun parseBasic(): Type =
        if (lexer.nextTokenIs(TYPE_OR_CTOR_NAME))
            parseTypeConcrete()
        else
            parseTypePrimitive()

    public fun parseTypePrimitive(): Type {
        if (lexer.nextTokenIs(LPAREN))
            return parseParens()
        else if (lexer.nextTokenIs(LBRACKET))
            return parseBrackets()
        else if (lexer.nextTokenIs(IDENTIFIER))
            return parseTypeVariable()
        else if (lexer.nextTokenIs(TYPE_OR_CTOR_NAME))
            return genericType(lexer.readTokenValue(TYPE_OR_CTOR_NAME)).sure()
        else
            throw lexer.expectFailure("type")
    }

    public fun parseTypeConcrete(): Type {
        val name = lexer.readTokenValue(TYPE_OR_CTOR_NAME)

        val args = ArrayList<Type?>()
        while (lexer.nextTokenIsOneOf(START_TOKENS))
            args.add(parseTypePrimitive())

        return genericType(name, args).sure()
    }

    private fun parseParens(): Type {
        lexer.expectToken(LPAREN)

        if (lexer.readMatchingToken(RPAREN))
            return Type.UNIT.sure()

        val types = ArrayList<Type?>()
        types.add(parseType())

        while (lexer.readMatchingToken(COMMA))
            types.add(parseType())

        lexer.expectToken(RPAREN)

        if (types.size() == 1)
            return types.get(0).sure()
        else
            return tupleType(types).sure()
    }

    private fun parseBrackets(): Type {
        lexer.expectToken(LBRACKET)
        val elementType = parseType()
        lexer.expectToken(RBRACKET)

        return listType(elementType).sure()
    }

    public fun parseTypeVariable(): TypeVariable {
        val name = lexer.readTokenValue(IDENTIFIER)
        return typeVariable(name).sure()
    }
}

