package komu.blunt.types;

import static komu.blunt.utils.Objects.requireNonNull;

import java.util.List;
import java.util.Set;

public final class TypeVariable extends Type {
    
    private final String name;
    private final Kind kind;

    public TypeVariable(String name, Kind kind) {
        this.name = requireNonNull(name);
        this.kind = requireNonNull(kind);
    }

    @Override
    protected String toString(final int precedence) {
        return name;
    }

    @Override
    public Kind getKind() {
        return kind;
    }

    @Override
    protected Type apply(Substitution substitution) {
        Type newType = substitution.lookup(this);
        return newType != null ? newType : this;
    }

    @Override
    protected Type instantiate(List<TypeVariable> vars) {
        return this;
    }

    @Override
    protected void addTypeVariables(Set<TypeVariable> result) {
        result.add(this);
    }
}
