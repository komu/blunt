package komu.blunt.ast;

import komu.blunt.objects.Symbol;

import static komu.blunt.utils.Objects.requireNonNull;

public final class ASTVariable extends ASTExpression {
    public final Symbol var;

    public ASTVariable(Symbol var) {
        this.var = requireNonNull(var);
    }

    @Override
    public String toString() {
        return var.toString();
    }
}
