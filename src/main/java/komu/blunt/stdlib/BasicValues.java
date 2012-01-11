package komu.blunt.stdlib;

import komu.blunt.objects.TypeConstructorValue;
import komu.blunt.types.DataTypeDefinitions;

@SuppressWarnings("unused")
public class BasicValues {

    public static final TypeConstructorValue FALSE = new TypeConstructorValue(0, DataTypeDefinitions.FALSE);
    public static final TypeConstructorValue TRUE = new TypeConstructorValue(1, DataTypeDefinitions.TRUE);
    public static final TypeConstructorValue LT = new TypeConstructorValue(0, "LT");
    public static final TypeConstructorValue EQ = new TypeConstructorValue(1, "EQ");
    public static final TypeConstructorValue GT = new TypeConstructorValue(2, "GT");
    public static final TypeConstructorValue UNIT = new TypeConstructorValue(0, DataTypeDefinitions.UNIT);

    public static TypeConstructorValue booleanToConstructor(boolean b) {
        return b ? TRUE : FALSE;
    }

    public static Object convertToJava(TypeConstructorValue value) {
        switch (value.name) {
        case "True":    return true;
        case "False":   return false;
        default:        return value;
        }
    }
}
