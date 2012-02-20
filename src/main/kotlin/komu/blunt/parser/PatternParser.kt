package komu.blunt.parser

import com.google.common.collect.ImmutableList
import com.google.common.collect.Lists
import komu.blunt.types.patterns.Pattern

import java.util.ArrayList
import java.util.Arrays
import java.util.List

import komu.blunt.parser.TokenType.*
import komu.blunt.types.ConstructorNames.*
import komu.blunt.types.patterns.Pattern.*
import java.util.Collections

final class PatternParser(val lexer: Lexer) {

    private val PATTERN_START_TOKENS =
        Arrays.asList(LPAREN, LBRACKET, LITERAL, IDENTIFIER, TYPE_OR_CTOR_NAME, UNDERSCORE).sure()

    // <literal> | <variable> | ( <pattern> ) | <constructor> <pattern>* |
    public fun parsePattern(): Pattern {
        if (lexer.nextTokenIs(TYPE_OR_CTOR_NAME)) {
            val name = lexer.readTokenValue(TYPE_OR_CTOR_NAME).sure()
            val args = ArrayList<Pattern?>()

            while (lexer.nextTokenIsOneOf(PATTERN_START_TOKENS))
                args.add(parseSimplePattern())

            return constructor(name, ImmutableList.copyOf(args)).sure()
        } else {
            return parseSimplePattern()
        }
    }

    public fun parseSimplePattern(): Pattern {
        var pattern = parsePrimitivePattern()

        if (lexer.nextTokenIs(OPERATOR) && lexer.peekTokenValue(OPERATOR).isConstructor) {
            val op = lexer.readTokenValue(OPERATOR)

            pattern = constructor(op.toString(), pattern, parsePattern()).sure()
        }

        return pattern
    }

    private fun parsePrimitivePattern(): Pattern {
        if (lexer.nextTokenIs(LITERAL)) {
            return literal(lexer.readTokenValue(LITERAL)).sure()

        } else if (lexer.readMatchingToken(UNDERSCORE)) {
            return wildcard().sure()

        } else if (lexer.nextTokenIs(IDENTIFIER)) {
            return variable(lexer.readTokenValue(IDENTIFIER)).sure()

        } else if (lexer.readMatchingToken(LPAREN)) {
            return parseParens()

        } else if (lexer.nextTokenIs(TYPE_OR_CTOR_NAME)) {
            return constructor(lexer.readTokenValue(TYPE_OR_CTOR_NAME)).sure()

        } else if (lexer.nextTokenIs(LBRACKET)) {
            return parseBrackets()


        } else {
            throw lexer.expectFailure("pattern")
        }
    }

    private fun parseBrackets(): Pattern {
        lexer.expectToken(LBRACKET)

        if (lexer.readMatchingToken(RBRACKET))
            return constructor(NIL).sure()

        val patterns = ArrayList<Pattern?>()

        patterns.add(parsePattern())
        while (lexer.readMatchingToken(COMMA))
            patterns.add(parsePattern())

        lexer.expectToken(RBRACKET)

        return createList(patterns)
    }

    private fun createList(patterns: List<Pattern?>): Pattern {
        var result = constructor(NIL).sure()

        for (val pattern in Lists.reverse(patterns))
            result = constructor(CONS, pattern, result).sure()

        return result
    }

    private fun parseParens(): Pattern {
        if (lexer.readMatchingToken(RPAREN))
            return constructor(UNIT).sure()

        val patterns = ArrayList<Pattern?>()

        do {
            patterns.add(parsePattern())
        } while (lexer.readMatchingToken(COMMA))

        lexer.expectToken(RPAREN)

        if (patterns.size() == 1)
            return patterns.get(0).sure()
        else
            return constructor(tupleName(patterns.size()), ImmutableList.copyOf(patterns)).sure()
    }
}
