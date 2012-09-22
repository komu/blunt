package komu.blunt.parser

import komu.blunt.parser.TokenType.*
import komu.blunt.types.ConstructorNames
import komu.blunt.types.patterns.Pattern

final class PatternParser(val lexer: Lexer) {

    private val PATTERN_START_TOKENS =
        arrayList(TokenType.LPAREN, TokenType.LBRACKET, TokenType.LITERAL, TokenType.IDENTIFIER, TokenType.TYPE_OR_CTOR_NAME, TokenType.UNDERSCORE)

    // <literal> | <variable> | ( <pattern> ) | <constructor> <pattern>* |
    public fun parsePattern(): Pattern {
        if (lexer.nextTokenIs(TokenType.TYPE_OR_CTOR_NAME)) {
            val name = lexer.readTokenValue(TokenType.TYPE_OR_CTOR_NAME)
            val args = listBuilder<Pattern>()

            while (lexer.nextTokenIsOneOf(PATTERN_START_TOKENS))
                args.add(parseSimplePattern())

            return Pattern.constructor(name, args.build())
        } else {
            return parseSimplePattern()
        }
    }

    public fun parseSimplePattern(): Pattern {
        var pattern = parsePrimitivePattern()

        if (lexer.nextTokenIs(TokenType.OPERATOR) && lexer.peekTokenValue(TokenType.OPERATOR).isConstructor) {
            val op = lexer.readTokenValue(TokenType.OPERATOR)

            pattern = Pattern.constructor(op.toString(), pattern, parsePattern())
        }

        return pattern
    }

    private fun parsePrimitivePattern(): Pattern =
        if (lexer.nextTokenIs(TokenType.LITERAL))
            Pattern.literal(lexer.readTokenValue(TokenType.LITERAL))
        else if (lexer.readMatchingToken(TokenType.UNDERSCORE))
            Pattern.wildcard()
        else if (lexer.nextTokenIs(TokenType.IDENTIFIER))
            Pattern.variable(lexer.readTokenValue(TokenType.IDENTIFIER))
        else if (lexer.readMatchingToken(TokenType.LPAREN))
            parseParens()
        else if (lexer.nextTokenIs(TokenType.TYPE_OR_CTOR_NAME))
            Pattern.constructor(lexer.readTokenValue(TokenType.TYPE_OR_CTOR_NAME))
        else if (lexer.nextTokenIs(TokenType.LBRACKET))
            parseBrackets()
        else
            throw lexer.expectFailure("pattern")

    private fun parseBrackets(): Pattern {
        lexer.expectToken(TokenType.LBRACKET)

        if (lexer.readMatchingToken(TokenType.RBRACKET))
            return Pattern.constructor(ConstructorNames.NIL)

        val patterns = listBuilder<Pattern>()

        patterns.add(parsePattern())
        while (lexer.readMatchingToken(TokenType.COMMA))
            patterns.add(parsePattern())

        lexer.expectToken(TokenType.RBRACKET)

        return createList(patterns.build())
    }

    private fun createList(patterns: List<Pattern>): Pattern {
        var result = Pattern.constructor(ConstructorNames.NIL)

        for (pattern in patterns.reverse())
            result = Pattern.constructor(ConstructorNames.CONS, pattern, result)

        return result
    }

    private fun parseParens(): Pattern {
        if (lexer.readMatchingToken(TokenType.RPAREN))
            return Pattern.constructor(ConstructorNames.UNIT)

        val patternsBuilder = listBuilder<Pattern>()

        do {
            patternsBuilder.add(parsePattern())
        } while (lexer.readMatchingToken(TokenType.COMMA))

        lexer.expectToken(TokenType.RPAREN)

        val patterns = patternsBuilder.build()
        if (patterns.size == 1)
            return patterns.first()
        else
            return Pattern.constructor(ConstructorNames.tupleName(patterns.size), patterns)
    }
}
