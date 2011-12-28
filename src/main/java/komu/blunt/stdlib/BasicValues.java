package komu.blunt.stdlib;

import komu.blunt.objects.TypeConstructorValue;

@SuppressWarnings("unused")
public class BasicValues {

    public static final TypeConstructorValue TRUE = new TypeConstructorValue("True");
    public static final TypeConstructorValue FALSE = new TypeConstructorValue("False");
    
    public static TypeConstructorValue booleanToConstructor(boolean b) {
        return b ? TRUE : FALSE;
    }

    public static Object convertToJava(TypeConstructorValue value) {
        if (value.name.equals("True")) {
            return true;
        } else if (value.name.equals("False")) {
            return false;
        } else {
            return value;
        }
    }
}
