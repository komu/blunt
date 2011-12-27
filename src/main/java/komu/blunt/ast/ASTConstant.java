package komu.blunt.ast;

import komu.blunt.core.CoreConstantExpression;
import komu.blunt.core.CoreExpression;
import komu.blunt.eval.StaticEnvironment;
import komu.blunt.types.Type;
import komu.blunt.types.TypeCheckResult;
import komu.blunt.types.TypeCheckingContext;

public final class ASTConstant extends ASTExpression {
    
    public final Object value;

    public ASTConstant(Object value) {
        this.value = value;
    }

    @Override
    public <R, C> R accept(ASTVisitor<C, R> visitor, C ctx) {
        return visitor.visit(this, ctx);
    }

    @Override
    public CoreExpression analyze(StaticEnvironment env) {
        return new CoreConstantExpression(value);
    }

    @Override
    public TypeCheckResult<Type> typeCheck(final TypeCheckingContext ctx) {
        return new TypeCheckResult<Type>(valueType());
    }
    
    private Type valueType() {
        return (value == null) ? Type.UNIT : Type.fromClass(value.getClass());
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
