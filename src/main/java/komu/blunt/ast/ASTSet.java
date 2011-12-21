package komu.blunt.ast;

import komu.blunt.objects.Symbol;

import static komu.blunt.utils.Objects.requireNonNull;

public final class ASTSet extends ASTExpression {
    public final Symbol var;
    public final ASTExpression exp;

    public ASTSet(Symbol var, ASTExpression exp) {
        this.var = requireNonNull(var);
        this.exp = requireNonNull(exp);
    }

    @Override
    public String toString() {
        return "(set! " + var + " " + exp + ")";
    }
}
