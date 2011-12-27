package komu.blunt.eval;

import komu.blunt.analyzer.AnalyzationException;
import komu.blunt.types.checker.UnificationException;

public class TypeCheckException extends AnalyzationException {

    public TypeCheckException(String message) {
        super(message);
    }

    public TypeCheckException(UnificationException e) {
        super(e.getMessage());
    }

}
