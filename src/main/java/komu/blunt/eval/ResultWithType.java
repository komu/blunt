package komu.blunt.eval;

import komu.blunt.types.Qualified;
import komu.blunt.types.Type;

public final class ResultWithType {
    
    public final Object result;
    public final Qualified<Type> type;
    
    public ResultWithType(Object result, Qualified<Type> type) {
        this.result = result;
        this.type = type;
    }

    @Override
    public String toString() {
        return type + ": " + result;
    }
}
