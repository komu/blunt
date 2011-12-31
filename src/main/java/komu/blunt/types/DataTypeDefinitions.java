package komu.blunt.types;

import komu.blunt.analyzer.AnalyzationException;
import komu.blunt.ast.ASTDataDefinition;

import java.util.*;

import static com.google.common.base.Strings.repeat;
import static java.util.Collections.unmodifiableCollection;
import static komu.blunt.parser.TypeParser.parseScheme;
import static komu.blunt.types.Type.functionType;
import static komu.blunt.types.Type.tupleType;

public class DataTypeDefinitions {
    public static final String CONS = ":";
    public static final String NIL = "[]";
    public static final String UNIT = "()";

    private final Map<String,ConstructorDefinition> constructors = new HashMap<String, ConstructorDefinition>(); 
    
    public DataTypeDefinitions() {
        register(UNIT, "()", 0);
        register(NIL, "[a]", 0);
        register(CONS, "a -> [a] -> [a]", 2);
    }
    
    private void register(String name, String scheme, int arity) {
        register(new ConstructorDefinition(name, parseScheme(scheme), arity));
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
        if (isTupleConstructor(name)) {
            int arity = tupleArity(name);
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
            types.add(Type.typeVariable("t" + i, Kind.STAR));

        return Qualified.quantifyAll(new Qualified<Type>(functionType(types, tupleType(types))));
    }

    public static int tupleArity(String name) {
        if (isTupleConstructor(name))
            return name.length()-1;
        else
            throw new IllegalArgumentException("not a tuple-type: " + name);
    }

    public static String tupleName(int arity) {
        return "(" + repeat(",", arity-1) + ")";
    }

    private static boolean isTupleConstructor(String name) {
        return name.matches("\\(,+\\)");
    }

    public Collection<ConstructorDefinition> getDeclaredConstructors() {
        return unmodifiableCollection(constructors.values());
    }
}
