package komu.blunt.parser;

import komu.blunt.types.Kind;
import komu.blunt.types.Type;
import komu.blunt.types.TypeVariable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static komu.blunt.parser.TokenType.*;
import static komu.blunt.types.Type.*;

final class TypeParser {

    private final Lexer lexer;

    @SuppressWarnings("unchecked")
    private static final List<TokenType<?>> START_TOKENS =
            Arrays.asList(LPAREN, LBRACKET, IDENTIFIER, TYPE_OR_CTOR_NAME);

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
            return parseConcrete();
        else
            return parseTypePrimitive();
    }

    private Type parseTypePrimitive() {
        if (lexer.nextTokenIs(LPAREN))
            return parseParens();
        else if (lexer.nextTokenIs(LBRACKET))
            return parseBrackets();
        else if (lexer.nextTokenIs(IDENTIFIER))
            return parseTypeVariable();
        else if (lexer.nextTokenIs(TYPE_OR_CTOR_NAME))
            return genericType(lexer.readToken(TYPE_OR_CTOR_NAME).value);
        else
            throw lexer.parseError("expected type");
    }

    private Type parseConcrete() {
        String name = lexer.readToken(TYPE_OR_CTOR_NAME).value;
        
        List<Type> args = new ArrayList<Type>();
        while (START_TOKENS.contains(lexer.peekTokenType()))
            args.add(parseTypePrimitive());
        
        return Type.genericType(name, args);
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
        String name = lexer.readToken(IDENTIFIER).value;
        return new TypeVariable(name, Kind.STAR);
    }
}
