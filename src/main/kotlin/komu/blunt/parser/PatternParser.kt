package komu.blunt.parser

import komu.blunt.objects.Symbol
import komu.blunt.types.ConstructorNames
import komu.blunt.types.patterns.Pattern
import java.util.*

final class PatternParser(val lexer: Lexer) {

    private val PATTERN_START_TOKENS =
        listOf(TokenType.LPAREN, TokenType.LBRACKET, TokenType.LITERAL, TokenType.IDENTIFIER, TokenType.TYPE_OR_CTOR_NAME, TokenType.UNDERSCORE)

    // <literal> | <variable> | ( <pattern> ) | <constructor> <pattern>* |
    fun parsePattern(): Pattern {
        if (lexer.nextTokenIs(TokenType.TYPE_OR_CTOR_NAME)) {
            val name = lexer.readTokenValue(TokenType.TYPE_OR_CTOR_NAME)
            val args = ArrayList<Pattern>()

            while (lexer.nextTokenIsOneOf(PATTERN_START_TOKENS))
                args.add(parseSimplePattern())

            return Pattern.Constructor(name, args)
        } else {
            return parseSimplePattern()
        }
    }

    fun parseSimplePattern(): Pattern {
        var pattern = parsePrimitivePattern()

        if (lexer.nextTokenIs(TokenType.OPERATOR) && lexer.peekTokenValue(TokenType.OPERATOR).isConstructor) {
            val op = lexer.readTokenValue(TokenType.OPERATOR)

            pattern = Pattern.Constructor(op.toString(), pattern, parsePattern())
        }

        return pattern
    }

    private fun parsePrimitivePattern(): Pattern =
        when {
            lexer.nextTokenIs(TokenType.LITERAL) ->
                Pattern.Literal(lexer.readTokenValue(TokenType.LITERAL))
            lexer.readMatchingToken(TokenType.UNDERSCORE) ->
                Pattern.Wildcard
            lexer.nextTokenIs(TokenType.IDENTIFIER) ->
                Pattern.Variable(Symbol(lexer.readTokenValue(TokenType.IDENTIFIER)))
            lexer.readMatchingToken(TokenType.LPAREN) ->
                parseParens()
            lexer.nextTokenIs(TokenType.TYPE_OR_CTOR_NAME) ->
                Pattern.Constructor(lexer.readTokenValue(TokenType.TYPE_OR_CTOR_NAME))
            lexer.nextTokenIs(TokenType.LBRACKET) ->
                parseBrackets()
            else ->
                lexer.expectFailure("pattern")
        }

    private fun parseBrackets(): Pattern {
        lexer.expectToken(TokenType.LBRACKET)

        if (lexer.readMatchingToken(TokenType.RBRACKET))
            return Pattern.Constructor(ConstructorNames.NIL)

        val patterns = ArrayList<Pattern>()

        patterns.add(parsePattern())
        while (lexer.readMatchingToken(TokenType.COMMA))
            patterns.add(parsePattern())

        lexer.expectToken(TokenType.RBRACKET)

        return createList(patterns)
    }

    private fun createList(patterns: List<Pattern>): Pattern {
        var result = Pattern.Constructor(ConstructorNames.NIL)

        for (pattern in patterns.reversed())
            result = Pattern.Constructor(ConstructorNames.CONS, pattern, result)

        return result
    }

    private fun parseParens(): Pattern {
        if (lexer.readMatchingToken(TokenType.RPAREN))
            return Pattern.Constructor(ConstructorNames.UNIT)

        val patternsBuilder = ArrayList<Pattern>()

        do {
            patternsBuilder.add(parsePattern())
        } while (lexer.readMatchingToken(TokenType.COMMA))

        lexer.expectToken(TokenType.RPAREN)

        val patterns = patternsBuilder
        if (patterns.size == 1)
            return patterns.first()
        else
            return Pattern.Constructor(ConstructorNames.tupleName(patterns.size), patterns)
    }
}
