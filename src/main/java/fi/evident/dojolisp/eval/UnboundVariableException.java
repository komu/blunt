package fi.evident.dojolisp.eval;

import fi.evident.dojolisp.objects.Symbol;

public class UnboundVariableException extends AnalyzationException {
    public UnboundVariableException(Symbol var) {
        super("unbound variable '" + var + "'");
    }
}
