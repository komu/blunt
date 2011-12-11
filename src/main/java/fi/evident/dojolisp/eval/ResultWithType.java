package fi.evident.dojolisp.eval;

import fi.evident.dojolisp.types.Type;

public final class ResultWithType {
    
    public final Object result;
    public final Type type;
    
    public ResultWithType(Object result, Type type) {
        this.result = result;
        this.type = type;
    }

    @Override
    public String toString() {
        return type + ": " + result;
    }
}
