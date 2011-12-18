package fi.evident.dojolisp.types;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class TypeConstructor extends Type {
    
    private final String name;
    private final Kind kind;
    
    public TypeConstructor(String name, Kind kind) {
        this.name = requireNonNull(name);
        this.kind = requireNonNull(kind);
    }

    @Override
    protected Type apply(Substitution substitution) {
        return this;
    }

    @Override
    protected Type instantiate(List<TypeVariable> vars) {
        return this;
    }

    @Override
    protected void addTypeVariables(Set<TypeVariable> result) {
    }

    @Override
    protected Kind getKind() {
        return kind;
    }

    @Override
    public String toString() {
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

    String toString(List<Type> arguments) {
        if (name.equals("->") && arguments.size() == 2) {
            return "(" + arguments.get(0) + " -> " + arguments.get(1) + ")";
        } else if (name.equals("ConsList") && arguments.size() == 1) {
            return "[" + arguments.get(0) + "]";
        } else if (name.equals(",")) {
            StringBuilder sb = new StringBuilder();
            sb.append("(");
            for (Iterator<Type> iterator = arguments.iterator(); iterator.hasNext(); ) {
                sb.append(iterator.next().toString());
                if (iterator.hasNext())
                    sb.append(", ");
            }
            sb.append(")");
            return sb.toString();
        } else {
            return "(" + name + " " + arguments + ")";
        }
    }
}
