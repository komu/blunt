package komu.blunt.ast;

import komu.blunt.objects.Symbol;

import static com.google.common.base.Preconditions.checkNotNull;
import static komu.blunt.objects.Symbol.symbol;

public final class ASTVariable extends ASTExpression {
    public final Symbol var;

    public ASTVariable(Symbol var) {
        this.var = checkNotNull(var);
    }
    
    public ASTVariable(String var) {
        this.var = symbol(var);
    }

    @Override
    public <R, C> R accept(ASTVisitor<C, R> visitor, C ctx) {
        return visitor.visit(this, ctx);
    }

    @Override
    public String toString() {
        return var.toString();
    }
}
