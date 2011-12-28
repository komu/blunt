package komu.blunt.ast;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ASTConstructor extends ASTExpression {
    
    private final String name;

    public ASTConstructor(String name) {
        this.name = checkNotNull(name);
    }
    
    @Override
    public <R, C> R accept(ASTVisitor<C, R> visitor, C ctx) {
        if (name.equals("True"))
            return new ASTConstant(true).accept(visitor, ctx);
        else if (name.equals("False"))
            return new ASTConstant(false).accept(visitor, ctx);
        else if (name.equals("Nothing"))
            return new ASTVariable("primitiveNothing").accept(visitor, ctx);
        else if (name.equals("Just"))
            return new ASTVariable("primitiveJust").accept(visitor, ctx);
        else
            throw new UnsupportedOperationException("unknown constructor: " + name);
    }

    @Override
    public String toString() {
        return name;
    }
}
