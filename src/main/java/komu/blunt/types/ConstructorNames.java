package komu.blunt.types;

import static com.google.common.base.Strings.repeat;

public class ConstructorNames {

    public static final String CONS = ":";
    public static final String NIL = "[]";
    public static final String UNIT = "()";
    public static final String TRUE = "True";
    public static final String FALSE = "False";

    public static String tupleName(int arity) {
        return "(" + repeat(",", arity-1) + ")";
    }
}
