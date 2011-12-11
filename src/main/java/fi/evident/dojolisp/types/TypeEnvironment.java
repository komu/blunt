package fi.evident.dojolisp.types;

import fi.evident.dojolisp.eval.TypeCheckException;

import java.util.List;

import static fi.evident.dojolisp.types.Type.ANY;

public class TypeEnvironment {
    
    public void assign(Type left, Type right) {
        unify(left, right);
    }    
    
    public Type unify(Type left, Type right) {
        if (left instanceof TypeVariable && right instanceof TypeVariable)
            return unifyVariables((TypeVariable) left, (TypeVariable) right);
        else if (left instanceof TypeVariable)
            return unifyVariable((TypeVariable) left, right);
        else if (right instanceof TypeVariable)
            return unifyVariable((TypeVariable) right, left);
        else if (left == ANY || right == ANY)
            return ANY;
        else if (left.equals(right))
            return left;
        else
            throw new TypeCheckException(left, right);
    }

    private Type unifyVariables(TypeVariable left, TypeVariable right) {
        if (left.isAssigned())
            return unifyVariable(right, left.getAssignedType());
        else if (right.isAssigned())
            return unifyVariable(left, right.getAssignedType());

        left.assign(right);
        return left;
    }

    private Type unifyVariable(TypeVariable var, Type type) {
        if (var.isAssigned())
            return unify(var.getAssignedType(), type);

        var.assign(type);
        return type;
    }

    public Type call(Type func, List<Type> argTypes) {
        return func.asFunctionType().typeCheckCall(this, argTypes);
    }

    public TypeEnvironment extend(List<Type> arguments) {
        return this;
    }
}
