package komu.blunt.types;

import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public final class TypeVariable extends Type {
    
    private final String name;
    private final Kind kind;

    public TypeVariable(String name, Kind kind) {
        this.name = checkNotNull(name);
        this.kind = checkNotNull(kind);
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
