package komu.blunt.parser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import komu.blunt.types.patterns.Pattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static komu.blunt.parser.TokenType.*;
import static komu.blunt.types.DataTypeDefinitions.*;
import static komu.blunt.types.patterns.Pattern.*;

final class PatternParser {

    private final Lexer lexer;

    @SuppressWarnings("unchecked")
    private static final List<TokenType<?>> PATTERN_START_TOKENS =
        Arrays.asList(LPAREN, LBRACKET, LITERAL, IDENTIFIER, TYPE_OR_CTOR_NAME, UNDERSCORE);

    PatternParser(Lexer lexer) {
        this.lexer = checkNotNull(lexer);
    }

    // <literal> | <variable> | ( <pattern> ) | <constructor> <pattern>* |
    public Pattern parsePattern() {
        if (lexer.nextTokenIs(TYPE_OR_CTOR_NAME)) {
            String name = lexer.readTokenValue(TYPE_OR_CTOR_NAME);
            ImmutableList.Builder<Pattern> args = ImmutableList.builder();

            while (lexer.nextTokenIsOneOf(PATTERN_START_TOKENS))
                args.add(parseSimplePattern());

            return constructor(name, args.build());
        } else {
            return parseSimplePattern();
        }
    }

    public Pattern parseSimplePattern() {
        Pattern pattern = parsePrimitivePattern();

        if (lexer.nextTokenIs(OPERATOR) && lexer.peekTokenValue(OPERATOR).isConstructor()) {
            Operator op = lexer.readTokenValue(OPERATOR);

            pattern = constructor(op.toString(), pattern, parsePattern());
        }

        return pattern;
    }

    private Pattern parsePrimitivePattern() {
        if (lexer.nextTokenIs(LITERAL)) {
            return literal(lexer.readTokenValue(LITERAL));

        } else if (lexer.readMatchingToken(UNDERSCORE)) {
            return wildcard();

        } else if (lexer.nextTokenIs(IDENTIFIER)) {
            return variable(lexer.readTokenValue(IDENTIFIER));

        } else if (lexer.readMatchingToken(LPAREN)) {
            return parseParens();

        } else if (lexer.nextTokenIs(TYPE_OR_CTOR_NAME)) {
            return constructor(lexer.readTokenValue(TYPE_OR_CTOR_NAME));

        } else if (lexer.nextTokenIs(LBRACKET)) {
            return parseBrackets();


        } else {
            throw lexer.expectFailure("pattern");
        }
    }

    private Pattern parseBrackets() {
        lexer.expectToken(LBRACKET);

        if (lexer.readMatchingToken(RBRACKET))
            return constructor(NIL);
        
        List<Pattern> patterns = new ArrayList<>();

        patterns.add(parsePattern());
        while (lexer.readMatchingToken(COMMA))
            patterns.add(parsePattern());

        lexer.expectToken(RBRACKET);

        return createList(patterns);
    }

    private Pattern createList(List<Pattern> patterns) {
        Pattern result = constructor(NIL);

        for (Pattern pattern : Lists.reverse(patterns))
            result = constructor(CONS, pattern, result);

        return result;
    }

    private Pattern parseParens() {
        if (lexer.readMatchingToken(RPAREN))
            return constructor(UNIT);

        ImmutableList.Builder<Pattern> patterns = ImmutableList.builder();

        do {
            patterns.add(parsePattern());
        } while (lexer.readMatchingToken(COMMA));

        lexer.expectToken(RPAREN);

        ImmutableList<Pattern> pts = patterns.build();
        if (pts.size() == 1)
            return pts.get(0);
        else
            return constructor(tupleName(pts.size()), pts);
    }
}
