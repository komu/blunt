package komu.blunt.types;

import komu.blunt.analyzer.AnalyzationException;
import komu.blunt.ast.ASTDataDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Strings.repeat;
import static komu.blunt.types.Type.functionType;
import static komu.blunt.types.Type.tupleType;

public class DataTypeDefinitions {
    public static final String CONS = ":";
    public static final String NIL = "[]";

    private final Map<String,ConstructorDefinition> constructors = new HashMap<String, ConstructorDefinition>(); 
    
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
        if (name.matches("\\(,+\\)")) {
            int arity = name.length()-2;
            return new ConstructorDefinition(name, tupleConstructorScheme(arity), arity);
        }
        ConstructorDefinition ctor = constructors.get(name);
        if (ctor != null)
            return ctor;
        else
            throw new AnalyzationException("unknown type constructor: " + name);
    }

    private static Scheme tupleConstructorScheme(int arity) {
        List<Type> types = new ArrayList<Type>(arity);
        for (int i = 0; i < arity; i++)
            types.add(TypeVariable.tyVar("t" + i, Kind.STAR));

        return Qualified.quantifyAll(new Qualified<Type>(functionType(types, tupleType(types))));
    }

    public static String tupleName(int arity) {
        return "(" + repeat(",", arity) + ")";
    }
}
