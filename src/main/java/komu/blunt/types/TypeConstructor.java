package komu.blunt.types;

import komu.blunt.types.checker.Substitution;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public final class TypeConstructor extends Type {
    
    private final String name;
    private final Kind kind;
    
    public TypeConstructor(String name, Kind kind) {
        this.name = checkNotNull(name);
        this.kind = checkNotNull(kind);
    }

    @Override
    public TypeConstructor apply(Substitution substitution) {
        return this;
    }

    @Override
    public boolean hnf() {
        return false;
    }

    @Override
    protected Type instantiate(List<TypeVariable> vars) {
        return this;
    }

    @Override
    public void addTypeVariables(Set<TypeVariable> result) {
    }

    @Override
    public Kind getKind() {
        return kind;
    }

    @Override
    protected String toString(final int precedence) {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;

        if (obj instanceof TypeConstructor) {
            TypeConstructor rhs = (TypeConstructor) obj;

            return name.equals(rhs.name)
                && kind.equals(rhs.kind);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode() * 79 + kind.hashCode();
    }

    String toString(List<Type> arguments, int precedence) {
        if (name.equals("->") && arguments.size() == 2) {
            return functionToString(arguments, precedence);

        } else if (name.equals("[]") && arguments.size() == 1) {
            return "[" + arguments.get(0) + "]";

        } else if (name.equals(",")) {
            return tupleToString(arguments);

        } else {
            return defaultToString(arguments, precedence);
        }
    }

    private String defaultToString(List<Type> arguments, int precedence) {
        StringBuilder sb = new StringBuilder();
        
        if (precedence != 0) sb.append("(");
        sb.append(name);
        
        for (Type arg : arguments)
            sb.append(' ').append(arg.toString(1));

        if (precedence != 0) sb.append(")");
        
        return sb.toString();
    }

    private String tupleToString(List<Type> arguments) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (Iterator<Type> iterator = arguments.iterator(); iterator.hasNext(); ) {
            sb.append(iterator.next().toString(0));
            if (iterator.hasNext())
                sb.append(", ");
        }
        sb.append(")");
        return sb.toString();
    }

    private String functionToString(List<Type> arguments, int precedence) {
        StringBuilder sb = new StringBuilder();

        if (precedence != 0) sb.append("(");
        sb.append(arguments.get(0).toString(1)).append(" -> ").append(arguments.get(1).toString(0));
        if (precedence != 0) sb.append(")");

        return sb.toString();
    }
}
