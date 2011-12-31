package komu.blunt.parser;

import com.google.common.collect.ImmutableList;
import komu.blunt.types.DataTypeDefinitions;
import komu.blunt.types.patterns.Pattern;

import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static komu.blunt.parser.TokenType.*;
import static komu.blunt.types.DataTypeDefinitions.UNIT;
import static komu.blunt.types.DataTypeDefinitions.tupleName;
import static komu.blunt.types.patterns.Pattern.*;

final class PatternParser {

    private final Lexer lexer;

    @SuppressWarnings("unchecked")
    private static final List<TokenType<?>> PATTERN_START_TOKENS =
        Arrays.asList(LPAREN, LBRACKET, LITERAL, IDENTIFIER, TYPE_OR_CTOR_NAME);

    PatternParser(Lexer lexer) {
        this.lexer = checkNotNull(lexer);
    }

    // <literal> | <variable> | ( <pattern> ) | <constructor> <pattern>* |
    public Pattern parsePattern() {
        if (lexer.nextTokenIs(TYPE_OR_CTOR_NAME)) {
            String name = lexer.readToken(TYPE_OR_CTOR_NAME).value;
            ImmutableList.Builder<Pattern> args = ImmutableList.builder();

            while (PATTERN_START_TOKENS.contains(lexer.peekTokenType()))
                args.add(parseSimplePattern());

            return constructor(name, args.build());
        } else {
            return parseSimplePattern();
        }
    }

    private Pattern parseSimplePattern() {
        Pattern pattern = parsePrimitivePattern();

        if (lexer.nextTokenIs(OPERATOR) && lexer.peekToken(OPERATOR).value.isConstructor()) {
            Operator op = lexer.readToken(OPERATOR).value;

            pattern = constructor(op.toString(), pattern, parsePattern());
        }

        return pattern;
    }

    private Pattern parsePrimitivePattern() {
        if (lexer.nextTokenIs(LITERAL)) {
            return literal(lexer.readToken(LITERAL).value);

        } else if (lexer.nextTokenIs(IDENTIFIER)) {
            String name = lexer.readToken(IDENTIFIER).value;
            if (name.equals("_"))
                return wildcard();
            else
                return variable(name);

        } else if (lexer.readMatchingToken(LPAREN)) {
            return parseParens();

        } else if (lexer.nextTokenIs(TYPE_OR_CTOR_NAME)) {
            return constructor(lexer.readToken(TYPE_OR_CTOR_NAME).value);

        } else if (lexer.readMatchingToken(LBRACKET)) {
            return parseBrackets();


        } else {
            throw lexer.parseError("expected pattern, got " + lexer.readToken());
        }
    }

    private Pattern parseBrackets() {
        // TODO: support non-empty lists
        lexer.expectToken(RBRACKET);
        return constructor(DataTypeDefinitions.NIL);
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
