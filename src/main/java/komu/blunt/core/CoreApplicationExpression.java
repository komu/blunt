package komu.blunt.core;

import komu.blunt.asm.Instructions;
import komu.blunt.asm.Linkage;
import komu.blunt.asm.Register;

import static com.google.common.base.Preconditions.checkNotNull;

public final class CoreApplicationExpression extends CoreExpression {
    
    private final CoreExpression func;
    private final CoreExpression arg;

    public CoreApplicationExpression(CoreExpression func, CoreExpression arg) {
        this.func = checkNotNull(func);
        this.arg = checkNotNull(arg);
    }

    @Override
    public void assemble(Instructions instructions, Register target, Linkage linkage) {
        func.assemble(instructions, Register.PROCEDURE, Linkage.NEXT);
        instructions.pushRegister(Register.PROCEDURE);

        arg.assemble(instructions, Register.ARG, Linkage.NEXT);

        instructions.popRegister(Register.PROCEDURE);

        // TODO: tail calls

        instructions.apply();
        if (target != Register.VAL)
            instructions.copy(target, Register.VAL);

        instructions.finishWithLinkage(linkage);
    }

    @Override
    public String toString() {
        return "(" + func + " " + arg + ")";
    }
}
