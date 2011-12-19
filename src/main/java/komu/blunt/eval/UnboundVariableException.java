package komu.blunt.eval;

import komu.blunt.objects.Symbol;

public class UnboundVariableException extends AnalyzationException {
    public UnboundVariableException(Symbol var) {
        super("unbound variable '" + var + "'");
    }
}
