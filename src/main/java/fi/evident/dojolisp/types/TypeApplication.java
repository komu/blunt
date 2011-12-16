package fi.evident.dojolisp.types;

import fi.evident.dojolisp.eval.TypeCheckException;

import java.util.List;
import java.util.Set;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class TypeApplication extends Type {
    
    final Type left;
    final Type right;

    public TypeApplication(Type left, Type right) {
        this.left = requireNonNull(left);
        this.right = requireNonNull(right);
    }

    @Override
    protected Type apply(Substitution substitution) {
        return new TypeApplication(left.apply(substitution), right.apply(substitution));
    }

    @Override
    public Type instantiate(List<TypeVariable> vars) {
        return new TypeApplication(left.instantiate(vars), right.instantiate(vars));
    }

    @Override
    protected void addTypeVariables(Set<TypeVariable> result) {
        left.addTypeVariables(result);
        right.addTypeVariables(result);
    }

    @Override
    protected Kind getKind() {
        Kind kind = left.getKind();
        if (kind instanceof ArrowKind)
            return ((ArrowKind) kind).right;
        else
            throw new TypeCheckException("invalid kind: " + left);
    }

    @Override
    public String toString() {
        return "(" + left + " " + right + ")";
    }
}
