package komu.blunt.ast;

import komu.blunt.objects.Symbol;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ASTVariable extends ASTExpression {
    public final Symbol var;

    public ASTVariable(Symbol var) {
        this.var = checkNotNull(var);
    }

    @Override
    public String toString() {
        return var.toString();
    }
}
