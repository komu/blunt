package komu.blunt.ast;

import komu.blunt.objects.Symbol;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ASTBinding {
    public final Symbol name;
    public final ASTExpression expr;

    public ASTBinding(Symbol name, ASTExpression expr) {
        this.name = checkNotNull(name);
        this.expr = checkNotNull(expr);
    }

    @Override
    public String toString() {
        return "[" + name + " " + expr + "]";
    }
}
