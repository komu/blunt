package komu.blunt.types;

import static komu.blunt.types.Type.functionType;
import static komu.blunt.types.Type.genericType;
import static komu.blunt.types.Type.listType;
import static komu.blunt.types.TypeVariable.tyVar;

import komu.blunt.analyzer.AnalyzationException;

public class DataTypeDefinitions {
    public static final String CONS = ":";
    public static final String NIL = "[]";

    // TODO: make this non-static
    public static ConstructorDefinition findConstructor(String name) {
        if (name.equals("True"))
            return new ConstructorDefinition(name, Type.BOOLEAN.toScheme(), "primitiveTrue");
        if (name.equals("False"))
            return new ConstructorDefinition(name, Type.BOOLEAN.toScheme(), "primitiveFalse");
        if (name.equals("Nothing")) 
            return new ConstructorDefinition(name, nothingType(), "primitiveNothing");
        if (name.equals("Just"))
            return new ConstructorDefinition(name, justType(), "primitiveJust");
        if (name.equals(NIL))
            return new ConstructorDefinition(name, nilType(), "primitiveNil");
        if (name.equals(CONS))
            return new ConstructorDefinition(name, consType(), "mkCons");

        throw new AnalyzationException("unknown type constructor: " + name);
    }

    private static Scheme nothingType() {
        TypeVariable var = tyVar("a", Kind.STAR);
        return Qualified.quantify(var, new Qualified<Type>(genericType("Maybe", var)));
    }

    private static Scheme nilType() {
        TypeVariable var = tyVar("a", Kind.STAR);
        return Qualified.quantify(var, new Qualified<Type>(listType(var)));
    }
    
    private static Scheme justType() {
        TypeVariable var = tyVar("a", Kind.STAR);
        return Qualified.quantify(var, new Qualified<Type>(functionType(var, genericType("Maybe", var))));
    }

    private static Scheme consType() {
        TypeVariable var = tyVar("a", Kind.STAR);
        return Qualified.quantify(var, new Qualified<Type>(functionType(var, functionType(listType(var), listType(var)))));
    }
}
