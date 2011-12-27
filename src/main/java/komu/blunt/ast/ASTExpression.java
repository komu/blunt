package komu.blunt.ast;

public abstract class ASTExpression {

    public abstract <R,C> R accept(ASTVisitor<C,R> visitor, C ctx);

    @Override
    public abstract String toString();
}
