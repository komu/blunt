package komu.blunt.parser;

import komu.blunt.types.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static komu.blunt.parser.TokenType.*;
import static komu.blunt.types.Type.*;

public final class TypeParser {

    private final Lexer lexer;

    @SuppressWarnings("unchecked")
    private static final List<TokenType<?>> START_TOKENS =
            Arrays.asList(LPAREN, LBRACKET, IDENTIFIER, TYPE_OR_CTOR_NAME);

    public static Type parseType(String s) {
        return new TypeParser(new Lexer(s)).parseType();
    }

    public static Scheme parseScheme(String s) {
        Qualified<Type> type = parseQualified(s);
        return Qualified.quantifyAll(type);
    }
    
    public static Qualified<Type> parseQualified(String s) {
        TypeParser parser = new TypeParser(new Lexer(s));
        return parser.parseQualified();
    }

    private Qualified<Type> parseQualified() {
        List<Predicate> predicates = parseOptionalPredicates();
        Type type = parseType();
        return new Qualified<Type>(predicates, type);
    }

    private List<Predicate> parseOptionalPredicates() {
        LexerState lexerState = lexer.save();
        try {
            List<Predicate> predicates = new ArrayList<Predicate>();
            if (lexer.readMatchingToken(LPAREN)) {
                do {
                    predicates.add(parsePredicate());
                } while (lexer.readMatchingToken(COMMA));
                lexer.expectToken(RPAREN);
            } else {
                predicates.add(parsePredicate());
            }
            lexer.expectToken(TokenType.BIG_RIGHT_ARROW);
            return predicates;
        } catch (SyntaxException e) {
            lexer.restore(lexerState);
            return Collections.emptyList();
        }
    }

    private Predicate parsePredicate() {
        String className = lexer.readTokenValue(TYPE_OR_CTOR_NAME);
        Type type = parseType();
        return Predicate.isIn(className, type);
    }

    TypeParser(Lexer lexer) {
        this.lexer = checkNotNull(lexer);
    }
    
    public Type parseType() {
        Type type = parseBasic();
        
        while (lexer.readMatchingToken(RIGHT_ARROW))
            type = functionType(type, parseType());

        return type;
    }

    private Type parseBasic() {
        if (lexer.nextTokenIs(TYPE_OR_CTOR_NAME))
            return parseTypeConcrete();
        else
            return parseTypePrimitive();
    }

    public Type parseTypePrimitive() {
        if (lexer.nextTokenIs(LPAREN))
            return parseParens();
        else if (lexer.nextTokenIs(LBRACKET))
            return parseBrackets();
        else if (lexer.nextTokenIs(IDENTIFIER))
            return parseTypeVariable();
        else if (lexer.nextTokenIs(TYPE_OR_CTOR_NAME))
            return genericType(lexer.readTokenValue(TYPE_OR_CTOR_NAME));
        else
            throw lexer.expectFailure("type");
    }

    public Type parseTypeConcrete() {
        String name = lexer.readTokenValue(TYPE_OR_CTOR_NAME);
        
        List<Type> args = new ArrayList<Type>();
        while (lexer.nextTokenIsOneOf(START_TOKENS))
            args.add(parseTypePrimitive());
        
        return genericType(name, args);
    }

    private Type parseParens() {
        lexer.expectToken(LPAREN);

        if (lexer.readMatchingToken(RPAREN))
            return Type.UNIT;

        List<Type> types = new ArrayList<Type>();
        types.add(parseType());
        
        while (lexer.readMatchingToken(COMMA))
            types.add(parseType());
        
        lexer.expectToken(RPAREN);

        if (types.size() == 1)
            return types.get(0);
        else
            return tupleType(types);
    }

    private Type parseBrackets() {
        lexer.expectToken(LBRACKET);
        Type elementType = parseType();
        lexer.expectToken(RBRACKET);

        return listType(elementType);
    }
    
    public TypeVariable parseTypeVariable() {
        String name = lexer.readTokenValue(IDENTIFIER);
        return typeVariable(name);
    }
}
