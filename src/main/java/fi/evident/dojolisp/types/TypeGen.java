package fi.evident.dojolisp.types;

import java.util.List;
import java.util.Set;

public final class TypeGen extends Type {
    
    private final int index;

    public TypeGen(int index) {
        this.index = index;
    }

    @Override
    protected Type apply(Substitution substitution) {
        return this;
    }

    @Override
    public Type instantiate(List<TypeVariable> vars) {
        return vars.get(index);
    }

    @Override
    protected void addTypeVariables(Set<TypeVariable> result) {
        // no variables here
    }

    @Override
    protected Kind getKind() {
        throw new RuntimeException("can't access kind of TypeGen");
    }
}
