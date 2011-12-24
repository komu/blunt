package komu.blunt.parser;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Operator {

    public static final Operator EQUAL = new Operator("=");
    public static final Operator LT = new Operator("<");
    public static final Operator LE = new Operator("<=");
    public static final Operator GT = new Operator(">");
    public static final Operator GE = new Operator(">=");
    public static final Operator PLUS = new Operator("+");
    public static final Operator MINUS = new Operator("-");
    public static final Operator MULTIPLY = new Operator("*");
    public static final Operator DIVIDE = new Operator("/");
    public static final Operator RIGHT_ARROW = new Operator("->");

    private final String name;

    public Operator(String name) {
        this.name = checkNotNull(name);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        
        if (obj instanceof Operator) {
            Operator rhs = (Operator) obj;
            
            return name.equals(rhs.name);
        }
        
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
