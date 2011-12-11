package fi.evident.dojolisp.types;

public class TypeVariable extends Type {
    
    private final int count;
    private Type assignedType;
    private static int countSequence = 0;

    public TypeVariable() {
        this.count = countSequence++;
    }

    public boolean isAssigned() {
        return assignedType != null;
    }

    public Type getAssignedType() {
        return assignedType;
    }

    public void assign(Type type) {
        this.assignedType = type;
    }

    @Override
    public String toString() {
        if (assignedType != null)
            return assignedType.toString();
        else
            return "$" + count;
    }
}
