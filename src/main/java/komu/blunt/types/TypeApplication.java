package komu.blunt.types;

import komu.blunt.eval.TypeCheckException;
import komu.blunt.types.checker.Substitution;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public final class TypeApplication extends Type {
    
    public final Type left;
    public final Type right;

    public TypeApplication(Type left, Type right) {
        this.left = checkNotNull(left);
        this.right = checkNotNull(right);
    }

    @Override
    public TypeApplication apply(Substitution substitution) {
        return new TypeApplication(left.apply(substitution), right.apply(substitution));
    }

    @Override
    protected Type instantiate(List<TypeVariable> vars) {
        return new TypeApplication(left.instantiate(vars), right.instantiate(vars));
    }

    @Override
    public boolean hnf() {
        return left.hnf();
    }

    @Override
    public void addTypeVariables(Set<TypeVariable> result) {
        left.addTypeVariables(result);
        right.addTypeVariables(result);
    }

    @Override
    public Kind getKind() {
        Kind kind = left.getKind();
        if (kind instanceof ArrowKind)
            return ((ArrowKind) kind).right;
        else
            throw new TypeCheckException("invalid kind: " + left);
    }

    @Override
    protected String toString(int precedence) {
        return toString(new LinkedList<Type>(), precedence);
    }

    private String toString(LinkedList<Type> arguments, int precedence) {
        arguments.addFirst(right);
        if (left instanceof TypeApplication) {
            return ((TypeApplication) left).toString(arguments, precedence);
            
        } else if (left instanceof TypeConstructor) {
            TypeConstructor ct = (TypeConstructor) left;
            return ct.toString(arguments, precedence);

        } else {
            return "(" + left + " " + arguments + ")";
        }
    }
}
