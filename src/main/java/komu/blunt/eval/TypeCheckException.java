package komu.blunt.eval;

import komu.blunt.types.UnificationException;

public class TypeCheckException extends AnalyzationException {

    public TypeCheckException(String message) {
        super(message);
    }

    public TypeCheckException(UnificationException e) {
        super(e.getMessage());
    }

}
