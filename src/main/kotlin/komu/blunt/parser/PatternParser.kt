package komu.blunt.parser

import com.google.common.collect.ImmutableList
import com.google.common.collect.Lists
import komu.blunt.types.patterns.Pattern

import java.util.ArrayList
import java.util.Arrays
import java.util.List

import komu.blunt.parser.TokenType.*
import komu.blunt.types.ConstructorNames
import java.util.Collections

final class PatternParser(val lexer: Lexer) {

    private val PATTERN_START_TOKENS =
        arrayList(TokenType.LPAREN, TokenType.LBRACKET, TokenType.LITERAL, TokenType.IDENTIFIER, TokenType.TYPE_OR_CTOR_NAME, TokenType.UNDERSCORE)

    // <literal> | <variable> | ( <pattern> ) | <constructor> <pattern>* |
    public fun parsePattern(): Pattern {
        if (lexer.nextTokenIs(TokenType.TYPE_OR_CTOR_NAME)) {
            val name = lexer.readTokenValue(TokenType.TYPE_OR_CTOR_NAME)
            val args = ArrayList<Pattern>()

            while (lexer.nextTokenIsOneOf(PATTERN_START_TOKENS))
                args.add(parseSimplePattern())

            return Pattern.constructor(name, ImmutableList.copyOf(args).sure())
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

        val patterns = ArrayList<Pattern>()

        patterns.add(parsePattern())
        while (lexer.readMatchingToken(TokenType.COMMA))
            patterns.add(parsePattern())

        lexer.expectToken(TokenType.RBRACKET)

        return createList(patterns)
    }

    private fun createList(patterns: List<Pattern>): Pattern {
        var result = Pattern.constructor(ConstructorNames.NIL)

        for (val pattern in Lists.reverse(patterns))
            result = Pattern.constructor(ConstructorNames.CONS, pattern, result)

        return result
    }

    private fun parseParens(): Pattern {
        if (lexer.readMatchingToken(TokenType.RPAREN))
            return Pattern.constructor(ConstructorNames.UNIT)

        val patterns = ArrayList<Pattern>()

        do {
            patterns.add(parsePattern())
        } while (lexer.readMatchingToken(TokenType.COMMA))

        lexer.expectToken(TokenType.RPAREN)

        if (patterns.size() == 1)
            return patterns.get(0)
        else
            return Pattern.constructor(ConstructorNames.tupleName(patterns.size()), ImmutableList.copyOf(patterns).sure())
    }
}
