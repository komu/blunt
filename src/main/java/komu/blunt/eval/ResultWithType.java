package komu.blunt.eval;

import komu.blunt.types.Type;

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
