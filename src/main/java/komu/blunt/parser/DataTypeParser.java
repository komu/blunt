package komu.blunt.parser;

import com.google.common.collect.ImmutableList;
import komu.blunt.ast.AST;
import komu.blunt.ast.ASTDataDefinition;
import komu.blunt.types.*;

import java.util.ArrayList;
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

        DataTypeBuilder builder = new DataTypeBuilder(lexer.readTokenValue(TYPE_OR_CTOR_NAME));
        
        while (!lexer.readMatchingToken(ASSIGN))
            builder.addVariable(typeParser.parseTypeVariable());

        do {
            parseConstructor(builder);
        } while (lexer.readMatchingToken(OR));

        lexer.expectToken(END);

        return builder.build();
    }

    private void parseConstructor(DataTypeBuilder builder) {
        String constructorName = lexer.readTokenValue(TYPE_OR_CTOR_NAME);

        List<Type> args = new ArrayList<Type>();
        while (!lexer.nextTokenIs(OR) && !lexer.nextTokenIs(END))
            args.add(typeParser.parseTypePrimitive());

        builder.addConstructor(constructorName, args);
    }

    private static final class DataTypeBuilder {

        private final String typeName;
        private final List<TypeVariable> vars = new ArrayList<TypeVariable>();
        private final ImmutableList.Builder<ConstructorDefinition> constructors = ImmutableList.builder();

        public DataTypeBuilder(String typeName) {
            this.typeName = checkNotNull(typeName);
        }
        
        public void addVariable(TypeVariable variable) {
            vars.add(checkNotNull(variable));
        }

        public void addConstructor(String constructorName, List<Type> args) {
            Type resultType = genericType(typeName, vars);
            Scheme scheme = quantify(vars, new Qualified<Type>(functionType(args, resultType)));
            constructors.add(new ConstructorDefinition(constructorName, scheme, args.size()));
        }

        public ASTDataDefinition build() {
            return AST.data(typeName, constructors.build());
        }
    }
}
