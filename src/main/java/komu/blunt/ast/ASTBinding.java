package komu.blunt.ast;

import komu.blunt.objects.Symbol;

import static komu.blunt.utils.Objects.requireNonNull;

public final class ASTBinding {
    public final Symbol name;
    public final ASTExpression expr;

    public ASTBinding(Symbol name, ASTExpression expr) {
        this.name = requireNonNull(name);
        this.expr = requireNonNull(expr);
    }

    @Override
    public String toString() {
        return "[" + name + " " + expr + "]";
    }
}
