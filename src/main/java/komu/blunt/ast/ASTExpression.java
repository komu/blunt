package komu.blunt.ast;

public abstract class ASTExpression {

    @Override
    public abstract String toString();

    public ASTExpression simplify() {
        return this;
    }
}
