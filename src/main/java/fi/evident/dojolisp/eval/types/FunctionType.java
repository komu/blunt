package fi.evident.dojolisp.eval.types;

import fi.evident.dojolisp.eval.TypeCheckException;

import java.util.Iterator;
import java.util.List;

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
        if (o == null || getClass() != o.getClass()) return false;

        FunctionType that = (FunctionType) o;

        if (argumentTypes != null ? !argumentTypes.equals(that.argumentTypes) : that.argumentTypes != null)
            return false;
        if (returnType != null ? !returnType.equals(that.returnType) : that.returnType != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = argumentTypes != null ? argumentTypes.hashCode() : 0;
        result = 31 * result + (returnType != null ? returnType.hashCode() : 0);
        return result;
    }

    public Type typeCheckCall(List<Type> paramTypes) {
        if (varArgs)
            typeCheckVarArgs(paramTypes);
        else
            typeCheckNonVarArgs(paramTypes);

        return returnType;
    }

    private void typeCheckNonVarArgs(List<Type> paramTypes) {
        if (argumentTypes.size() != paramTypes.size())
            throw new TypeCheckException("invalid call: expected " + argumentTypes + ", but got " + paramTypes);

        for (int i = 0; i < argumentTypes.size(); i++)
            argumentTypes.get(0).unify(paramTypes.get(i));
    }

    private void typeCheckVarArgs(List<Type> paramTypes) {
        if (paramTypes.size() < argumentTypes.size() - 1)
            throw new TypeCheckException("not enough arguments for var-args call (required " + argumentTypes.size() + ", but got " + paramTypes.size() + ")");

        for (int i = 0; i < argumentTypes.size() - 1; i++)
            argumentTypes.get(0).unify(paramTypes.get(i));
        
        Type varArgType = argumentTypes.get(argumentTypes.size()-1);
        List<Type> rest = paramTypes.subList(argumentTypes.size()-1, paramTypes.size());
        
        for (Type arg : rest)
            varArgType.unify(arg);
    }
}
