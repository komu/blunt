package komu.blunt.types;

import komu.blunt.analyzer.AnalyzationException;
import komu.blunt.ast.ASTDataDefinition;

import java.util.*;

import static com.google.common.base.Strings.repeat;
import static java.util.Collections.unmodifiableCollection;
import static komu.blunt.types.Qualified.quantifyAll;
import static komu.blunt.types.Type.*;

public class DataTypeDefinitions {
    public static final String CONS = ":";
    public static final String NIL = "[]";
    public static final String UNIT = "()";
    public static final String TRUE = "True";
    public static final String FALSE = "False";

    private final Map<String,ConstructorDefinition> constructors = new HashMap<>();
    
    public DataTypeDefinitions() {
        register(0, UNIT, "()", 0);
        register(0, NIL, "[a]", 0);
        register(1, CONS, "a -> [a] -> [a]", 2);

        // TODO: create necessary tuples on demand
        for (int arity = 2; arity < 30; arity++)
            register(new ConstructorDefinition(arity-2, tupleName(arity), tupleConstructorScheme(arity), arity));
    }
    
    private void register(int index, String name, String scheme, int arity) {
        throw new UnsupportedOperationException("porting");
        //register(new ConstructorDefinition(0, name, TypeParser.$classobj.parseScheme(scheme), arity));
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

    private static Scheme tupleConstructorScheme(int arity) {
        List<Type> types = new ArrayList<>(arity);
        for (int i = 0; i < arity; i++)
            types.add(typeVariable("t" + i));

        return quantifyAll(new Qualified<>(functionType(types, tupleType(types))));
    }

    public static String tupleName(int arity) {
        return "(" + repeat(",", arity-1) + ")";
    }

    public Collection<ConstructorDefinition> getDeclaredConstructors() {
        return unmodifiableCollection(constructors.values());
    }
}
