package komu.blunt.ast;

import komu.blunt.types.Type;

public final class ASTConstant extends ASTExpression {
    
    public final Object value;

    ASTConstant(Object value) {
        this.value = value;
    }

    public Type valueType() {
        return (value == null) ? Type.UNIT : Type.fromClass(value.getClass());
    }

    @Override
    public ASTExpression simplify() {
        return this;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
