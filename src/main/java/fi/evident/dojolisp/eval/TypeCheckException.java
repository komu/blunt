package fi.evident.dojolisp.eval;

import fi.evident.dojolisp.types.Type;

public class TypeCheckException extends AnalyzationException {
    public TypeCheckException(Type left, Type right) {
        super("type check failure: " + left + " != " + right);
    }

    public TypeCheckException(String message) {
        super(message);
    }
}
