package komu.blunt.ast;

import komu.blunt.core.CoreConstantExpression;
import komu.blunt.core.CoreExpression;
import komu.blunt.eval.RootBindings;
import komu.blunt.eval.StaticEnvironment;
import komu.blunt.types.*;

public final class ASTConstant extends ASTExpression {
    
    public final Object value;

    public ASTConstant(Object value) {
        this.value = value;
    }

    @Override
    public CoreExpression analyze(StaticEnvironment env, RootBindings rootBindings) {
        return new CoreConstantExpression(value);
    }

    @Override
    public TypeCheckResult<Type> typeCheck(ClassEnv ce, TypeChecker tc, Assumptions as) {
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
