package komu.blunt.core;

import komu.blunt.asm.Assembler;
import komu.blunt.asm.Instructions;
import komu.blunt.asm.Linkage;
import komu.blunt.asm.Register;

import static com.google.common.base.Preconditions.checkNotNull;

public final class CoreConstantExpression extends CoreExpression {

    private final Object value;

    public CoreConstantExpression(Object value) {
        this.value = checkNotNull(value);
    }

    @Override
    public void assemble(Assembler asm, Instructions instructions, Register target, Linkage linkage) {
        instructions.loadConstant(target, value);
        instructions.finishWithLinkage(linkage);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
