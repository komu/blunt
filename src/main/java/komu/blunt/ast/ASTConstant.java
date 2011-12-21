package komu.blunt.ast;

public final class ASTConstant extends ASTExpression {
    
    public final Object value;

    public ASTConstant(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
