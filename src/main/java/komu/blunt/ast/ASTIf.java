package komu.blunt.ast;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ASTIf extends ASTExpression {
    public final ASTExpression test;
    public final ASTExpression consequent;
    public final ASTExpression alternative;

    public ASTIf(ASTExpression test, ASTExpression consequent, ASTExpression alternative) {
        this.test = checkNotNull(test);
        this.consequent = checkNotNull(consequent);
        this.alternative = checkNotNull(alternative);
    }

    @Override
    public <R, C> R accept(ASTVisitor<C, R> visitor, C ctx) {
        return visitor.visit(this, ctx);
    }

    @Override
    public String toString() {
        return "(if " + test + " " + consequent + " " + alternative + ")";
    }
}
