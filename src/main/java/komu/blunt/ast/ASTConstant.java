package komu.blunt.ast;

import komu.blunt.types.Type;

public final class ASTConstant extends ASTExpression {
    
    public final Object value;

    ASTConstant(Object value) {
        this.value = value;
    }

    @Override
    public <R, C> R accept(ASTVisitor<C, R> visitor, C ctx) {
        return visitor.visit(this, ctx);
    }

    public Type valueType() {
        return (value == null) ? Type.UNIT : Type.fromClass(value.getClass());
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
