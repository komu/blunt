package fi.evident.dojolisp.types;

import fi.evident.dojolisp.eval.TypeCheckException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class FunctionType extends Type {

    private final List<Type> argumentTypes;
    private final Type returnType;
    private final boolean varArgs;
    
    public FunctionType(List<Type> argumentTypes, Type returnType, boolean varArgs) {
        this.argumentTypes = requireNonNull(argumentTypes);
        this.returnType = requireNonNull(returnType);
        this.varArgs = varArgs;
    }

    @Override
    public FunctionType asFunctionType() {
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("(");
        
        for (Iterator<Type> it = argumentTypes.iterator(); it.hasNext(); ) {
            sb.append(it.next());
            if (it.hasNext())
                sb.append(", ");
        }
        sb.append(") -> ");
        sb.append(returnType);
        
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o instanceof FunctionType) {
            FunctionType rhs = (FunctionType) o;
            
            return argumentTypes.equals(rhs.argumentTypes)
                || returnType.equals(rhs.returnType)
                || varArgs == rhs.varArgs;
        }
        
        return false;
    }

    @Override
    public int hashCode() {
        return argumentTypes.hashCode() * 31 + returnType.hashCode();
    }

    public Type typeCheckCall(TypeEnvironment env, List<Type> paramTypes) {
        if (varArgs)
            typeCheckVarArgs(env, paramTypes);
        else
            typeCheckNonVarArgs(env, paramTypes);

        return returnType;
    }

    private void typeCheckNonVarArgs(TypeEnvironment env, List<Type> paramTypes) {
        if (argumentTypes.size() != paramTypes.size())
            throw new TypeCheckException("invalid call: expected " + argumentTypes + ", but got " + paramTypes);

        for (int i = 0; i < argumentTypes.size(); i++)
            env.assign(argumentTypes.get(0), paramTypes.get(i));
    }

    private void typeCheckVarArgs(TypeEnvironment env, List<Type> paramTypes) {
        if (paramTypes.size() < argumentTypes.size() - 1)
            throw new TypeCheckException("not enough arguments for var-args call (required " + argumentTypes.size() + ", but got " + paramTypes.size() + ")");

        for (int i = 0; i < argumentTypes.size() - 1; i++)
            env.assign(argumentTypes.get(0), paramTypes.get(i));
        
        Type varArgType = argumentTypes.get(argumentTypes.size()-1);
        List<Type> rest = paramTypes.subList(argumentTypes.size()-1, paramTypes.size());
        
        for (Type arg : rest)
            env.assign(varArgType, arg);
    }

    @Override
    protected Kind getKind() {
        return Kind.STAR; // TODO
    }

    @Override
    protected Type apply(Substitution substitution) {
        List<Type> newArgumentTypes = new ArrayList<Type>(argumentTypes.size());
        for (Type argumentType : argumentTypes)
            newArgumentTypes.add(argumentType.apply(substitution));
        
        Type newReturnType = returnType.apply(substitution);
        
        return new FunctionType(newArgumentTypes, newReturnType, varArgs);
    }

    @Override
    public Type instantiate(List<TypeVariable> vars) {
        List<Type> newArgumentTypes = new ArrayList<Type>(argumentTypes.size());
        for (Type argumentType : argumentTypes)
            newArgumentTypes.add(argumentType.instantiate(vars));

        Type newReturnType = returnType.instantiate(vars);

        return new FunctionType(newArgumentTypes, newReturnType, varArgs);
    }

    @Override
    protected void addTypeVariables(Set<TypeVariable> result) {
        for (Type arg : argumentTypes)
            arg.addTypeVariables(result);

        returnType.addTypeVariables(result);
    }
}
