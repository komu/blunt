package komu.blunt.ast;

import komu.blunt.objects.Symbol;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ASTSet extends ASTExpression {
    public final Symbol var;
    public final ASTExpression exp;

    public ASTSet(Symbol var, ASTExpression exp) {
        this.var = checkNotNull(var);
        this.exp = checkNotNull(exp);
    }

    @Override
    public String toString() {
        return "(set! " + var + " " + exp + ")";
    }
}
