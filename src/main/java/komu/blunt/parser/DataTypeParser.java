package komu.blunt.parser;

import com.google.common.collect.ImmutableList;
import komu.blunt.ast.AST;
import komu.blunt.ast.ASTDataDefinition;
import komu.blunt.types.ConstructorDefinition;
import komu.blunt.types.Qualified;
import komu.blunt.types.Type;
import komu.blunt.types.TypeVariable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static komu.blunt.parser.TokenType.*;
import static komu.blunt.types.Qualified.quantify;
import static komu.blunt.types.Type.functionType;
import static komu.blunt.types.Type.genericType;

final class DataTypeParser {
    
    private final Lexer lexer;
    private final TypeParser typeParser;

    DataTypeParser(Lexer lexer, TypeParser typeParser) {
        this.lexer = checkNotNull(lexer);
        this.typeParser = checkNotNull(typeParser);
    }

    // data <type> <var>* = <constructor>+
    ASTDataDefinition parseDataDefinition() {
        lexer.expectIndentStartToken(DATA);

        String name = lexer.readTokenValue(TYPE_OR_CTOR_NAME);
        
        List<TypeVariable> vars = new ArrayList<TypeVariable>();
        while (!lexer.nextTokenIs(ASSIGN))
            vars.add(typeParser.parseTypeVariable());

        lexer.expectToken(ASSIGN);

        ImmutableList.Builder<ConstructorDefinition> constructors = ImmutableList.builder();
        
        Type resultType = genericType(name, vars);
        
        do {
            constructors.add(parseConstructorDefinition(vars, resultType));
        } while (lexer.readMatchingToken(OR));

        lexer.expectToken(END);

        return AST.data(name, constructors.build());
    }

    private ConstructorDefinition parseConstructorDefinition(Collection<TypeVariable> vars, Type resultType) {
        String name = lexer.readTokenValue(TYPE_OR_CTOR_NAME);
        
        List<Type> args = new ArrayList<Type>();
        while (!lexer.nextTokenIs(OR) && !lexer.nextTokenIs(END))
            args.add(typeParser.parseTypePrimitive());

        Qualified<Type> type = new Qualified<Type>(functionType(args, resultType));
        return new ConstructorDefinition(name, quantify(vars, type), args.size());
    }    
}
