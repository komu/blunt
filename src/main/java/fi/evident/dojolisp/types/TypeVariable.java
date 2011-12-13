package fi.evident.dojolisp.types;

import java.util.List;
import java.util.Set;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class TypeVariable extends Type {
    
    private final String name;
    private final Kind kind;
    private static int index = 0;

    public TypeVariable(String name, Kind kind) {
        this.name = requireNonNull(name);
        this.kind = requireNonNull(kind);
    }

    public static TypeVariable newVar(Kind kind) {
        return new TypeVariable("?v" + index++, kind);
    }

    @Override
    public String toString() {
        return name;
    }

    public Kind getKind() {
        return kind;
    }

    @Override
    protected Type apply(Substitution substitution) {
        Type newType = substitution.lookup(this);
        return newType != null ? newType : this;
    }

    @Override
    public Type instantiate(List<TypeVariable> vars) {
        return this;
    }

    @Override
    protected void addTypeVariables(Set<TypeVariable> result) {
        result.add(this);
    }
}
