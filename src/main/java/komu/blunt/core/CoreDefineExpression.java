package komu.blunt.core;

import komu.blunt.analyzer.VariableReference;
import komu.blunt.asm.Instructions;
import komu.blunt.asm.Linkage;
import komu.blunt.asm.Register;
import komu.blunt.stdlib.BasicValues;

import static com.google.common.base.Preconditions.checkNotNull;

public final class CoreDefineExpression extends CoreExpression {

    private final CoreExpression expression;
    private VariableReference var;

    public CoreDefineExpression(CoreExpression expression, VariableReference var) {
        this.expression = checkNotNull(expression);
        this.var = checkNotNull(var);
    }

    @Override
    public void assemble(Instructions instructions, Register target, Linkage linkage) {
        expression.assemble(instructions, target, Linkage.NEXT);

        instructions.storeVariable(var, target);

        instructions.loadConstant(target, BasicValues.UNIT);
        instructions.finishWithLinkage(linkage);
    }
}
