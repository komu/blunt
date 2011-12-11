package fi.evident.dojolisp.types;

import fi.evident.dojolisp.eval.TypeCheckException;

import java.util.List;

import static fi.evident.dojolisp.types.Type.ANY;

public class TypeEnvironment {
    
    public void assign(Type left, Type right) {
        unify(left, right);
    }    
    
    public Type unify(Type left, Type right) {
        if (left == ANY || right == ANY)
            return ANY;

        if (left.equals(right))
            return left;
        else
            throw new TypeCheckException(left, right);
    }

    public Type call(Type func, List<Type> argTypes) {
        return func.asFunctionType().typeCheckCall(this, argTypes);
    }

    public TypeEnvironment extend(List<Type> arguments) {
        return this;
    }
}
