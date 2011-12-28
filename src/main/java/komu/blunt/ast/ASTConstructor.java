package komu.blunt.ast;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ASTConstructor extends ASTExpression {
    
    private final String name;

    public ASTConstructor(String name) {
        this.name = checkNotNull(name);
    }
    
    @Override
    public <R, C> R accept(ASTVisitor<C, R> visitor, C ctx) {
        return new ASTVariable(name).accept(visitor, ctx);
    }

    @Override
    public String toString() {
        return name;
    }
}
