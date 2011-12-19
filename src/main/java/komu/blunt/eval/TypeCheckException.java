package komu.blunt.eval;

import komu.blunt.types.Type;

public class TypeCheckException extends AnalyzationException {
    public TypeCheckException(Type left, Type right) {
        super("type check failure: " + left + " != " + right);
    }

    public TypeCheckException(String message) {
        super(message);
    }
}
