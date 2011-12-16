package fi.evident.dojolisp.types;

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
    public Type instantiate(List<TypeVariable> vars) {
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
}
