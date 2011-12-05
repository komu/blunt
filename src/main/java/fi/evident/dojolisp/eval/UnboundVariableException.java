package fi.evident.dojolisp.eval;

import fi.evident.dojolisp.types.Symbol;

public class UnboundVariableException extends AnalyzationException {
    public UnboundVariableException(Symbol var) {
        super("unbound variable '" + var + "'");
    }
}
