package komu.blunt.types;

import java.util.List;
import java.util.Set;

public final class TypeGen extends Type {
    
    private final int index;

    public TypeGen(int index) {
        this.index = index;
    }

    @Override
    public TypeGen apply(Substitution substitution) {
        return this;
    }

    @Override
    protected Type instantiate(List<TypeVariable> vars) {
        return vars.get(index);
    }

    @Override
    public void addTypeVariables(Set<TypeVariable> result) {
        // no variables here
    }

    @Override
    protected Kind getKind() {
        throw new RuntimeException("can't access kind of TypeGen");
    }

    @Override
    public boolean hnf() {
        throw new RuntimeException("should not call hnf for TypeGen");
    }

    @Override
    protected String toString(final int precedence) {
        return "TypeGen[" + index + "]";
    }
}
