package komu.blunt.ast;

import static komu.blunt.utils.Objects.requireNonNull;

public final class ASTIf extends ASTExpression {
    public final ASTExpression test;
    public final ASTExpression consequent;
    public final ASTExpression alternative;

    public ASTIf(ASTExpression test, ASTExpression consequent, ASTExpression alternative) {
        this.test = requireNonNull(test);
        this.consequent = requireNonNull(consequent);
        this.alternative = requireNonNull(alternative);
    }

    @Override
    public String toString() {
        return "(if " + test + " " + consequent + " " + alternative + ")";
    }
}
