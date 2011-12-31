package komu.blunt.types;

import com.google.common.base.Objects;
import komu.blunt.types.checker.Substitution;

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
    public boolean hnf() {
        return true;
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
    public Type apply(Substitution substitution) {
        Type newType = substitution.lookup(this);
        return newType != null ? newType : this;
    }

    @Override
    protected Type instantiate(List<TypeVariable> vars) {
        return this;
    }

    @Override
    public void addTypeVariables(Set<TypeVariable> result) {
        result.add(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;

        if (obj instanceof TypeVariable) {
            TypeVariable rhs = (TypeVariable) obj;

            return name.equals(rhs.name)
                && kind.equals(rhs.kind);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, kind);
    }
}
