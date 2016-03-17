package komu.blunt.parser

import komu.blunt.objects.Symbol
import komu.blunt.parser.TokenType.Companion.COMMA
import komu.blunt.parser.TokenType.Companion.IDENTIFIER
import komu.blunt.parser.TokenType.Companion.LBRACKET
import komu.blunt.parser.TokenType.Companion.LITERAL
import komu.blunt.parser.TokenType.Companion.LPAREN
import komu.blunt.parser.TokenType.Companion.RBRACKET
import komu.blunt.parser.TokenType.Companion.RPAREN
import komu.blunt.parser.TokenType.Companion.TYPE_OR_CTOR_NAME
import komu.blunt.types.ConstructorNames
import komu.blunt.types.patterns.Pattern
import java.util.*

final class PatternParser(val lexer: Lexer) {

    private val PATTERN_START_TOKENS =
        listOf(LPAREN, LBRACKET, LITERAL, IDENTIFIER, TYPE_OR_CTOR_NAME, TokenType.UNDERSCORE)

    // <literal> | <variable> | ( <pattern> ) | <constructor> <pattern>* |
    fun parsePattern(): Pattern {
        if (lexer.nextTokenIs(TYPE_OR_CTOR_NAME)) {
            val name = lexer.readTokenValue(TYPE_OR_CTOR_NAME)
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
            lexer.nextTokenIs(LITERAL) ->
                Pattern.Literal(lexer.readTokenValue(LITERAL))
            lexer.readMatchingToken(TokenType.UNDERSCORE) ->
                Pattern.Wildcard
            lexer.nextTokenIs(IDENTIFIER) ->
                Pattern.Variable(Symbol(lexer.readTokenValue(IDENTIFIER)))
            lexer.nextTokenIs(LPAREN) ->
                parseParens()
            lexer.nextTokenIs(TYPE_OR_CTOR_NAME) ->
                Pattern.Constructor(lexer.readTokenValue(TYPE_OR_CTOR_NAME))
            lexer.nextTokenIs(LBRACKET) ->
                parseBrackets()
            else ->
                lexer.expectFailure("pattern")
        }

    private fun parseBrackets(): Pattern =
        lexer.inBrackets {
            if (lexer.nextTokenIs(RBRACKET))
                Pattern.Constructor(ConstructorNames.NIL)
            else
                createList(lexer.sepBy(TokenType.COMMA) { parsePattern() })
        }

    private fun createList(patterns: List<Pattern>): Pattern {
        var result = Pattern.Constructor(ConstructorNames.NIL)

        for (pattern in patterns.reversed())
            result = Pattern.Constructor(ConstructorNames.CONS, pattern, result)

        return result
    }

    private fun parseParens(): Pattern = lexer.inParens {
        if (lexer.nextTokenIs(RPAREN)) {
            Pattern.Constructor(ConstructorNames.UNIT)
        } else {
            val patterns = lexer.sepBy(COMMA) { parsePattern() }
            patterns.singleOrNull() ?: Pattern.Constructor(ConstructorNames.tupleName(patterns.size), patterns)
        }
    }
}
