package komu.blunt.core;

import komu.blunt.asm.Assembler;
import komu.blunt.asm.Instructions;
import komu.blunt.asm.Linkage;
import komu.blunt.asm.Register;

import static com.google.common.base.Preconditions.checkNotNull;

public class CoreEqualConstantExpression extends CoreExpression {
    private final Object value;
    private final CoreExpression expression;

    public CoreEqualConstantExpression(Object value, CoreExpression expression) {
        this.value = value;
        this.expression = checkNotNull(expression);
    }

    @Override
    public Instructions assemble(Assembler asm, Register target, Linkage linkage) {
        Instructions instructions = new Instructions();
        instructions.append(expression.assemble(asm, target, Linkage.NEXT));
        instructions.equalConstant(target, target, value);
        instructions.finishWithLinkage(linkage);
        return instructions;
    }

    @Override
    public String toString() {
        return "(= " + value + " " + expression + ")";
    }
}
