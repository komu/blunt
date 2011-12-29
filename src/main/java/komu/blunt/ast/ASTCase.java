package komu.blunt.ast;

import com.google.common.collect.ImmutableList;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ASTCase extends ASTExpression {

    public final ASTExpression exp;
    public final ImmutableList<ASTAlternative> alternatives;

    public ASTCase(ASTExpression exp, ImmutableList<ASTAlternative> alternatives) {
        this.exp = checkNotNull(exp);
        this.alternatives = checkNotNull(alternatives);
    }

    @Override
    public <R, C> R accept(ASTVisitor<C, R> visitor, C ctx) {
        return visitor.visit(this, ctx);
    }

    @Override
    public String toString() {
        return "case " + exp + " of " + alternatives;
    }
}
