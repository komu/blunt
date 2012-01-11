package komu.blunt.ast;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ASTConstructor extends ASTExpression {
    
    public final String name;

    ASTConstructor(String name) {
        this.name = checkNotNull(name);
    }
    
    @Override
    public <R, C> R accept(ASTVisitor<C, R> visitor, C ctx) {
        return visitor.visit(this, ctx);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public ASTExpression simplify() {
        return this;
    }
}
