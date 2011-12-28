package komu.blunt.types;

import komu.blunt.analyzer.AnalyzationException;
import komu.blunt.ast.ASTDataDefinition;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Strings.repeat;
import static komu.blunt.types.Type.functionType;
import static komu.blunt.types.Type.listType;
import static komu.blunt.types.TypeVariable.tyVar;

public class DataTypeDefinitions {
    public static final String CONS = ":";
    public static final String NIL = "[]";

    private final Map<String,ConstructorDefinition> constructors = new HashMap<String, ConstructorDefinition>(); 
    
    public DataTypeDefinitions() {
        register(new ConstructorDefinition(NIL, nilType(), "primitiveNil", 0));
        register(new ConstructorDefinition(CONS, consType(), "mkCons", 1));
    }

    public void register(ASTDataDefinition definition) {
        for (ConstructorDefinition constructor : definition.constructors)
            register(constructor);
    }

    public void register(ConstructorDefinition definition) {
        if (constructors.containsKey(definition.name))
            throw new AnalyzationException("duplicate type constructor: " + definition.name);

        constructors.put(definition.name, definition);
    }
    
    public ConstructorDefinition findConstructor(String name) {
        ConstructorDefinition ctor = constructors.get(name);
        if (ctor != null)
            return ctor;
        else
            throw new AnalyzationException("unknown type constructor: " + name);
    }
    
    public static String tupleName(int arity) {
        return "(" + repeat(",", arity) + ")";
    }

    private static Scheme nilType() {
        TypeVariable var = tyVar("a", Kind.STAR);
        return Qualified.quantify(var, new Qualified<Type>(listType(var)));
    }
    
    private static Scheme consType() {
        TypeVariable var = tyVar("a", Kind.STAR);
        return Qualified.quantify(var, new Qualified<Type>(functionType(var, functionType(listType(var), listType(var)))));
    }
}
