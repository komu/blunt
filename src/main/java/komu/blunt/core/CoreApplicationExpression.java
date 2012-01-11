package komu.blunt.core;

import komu.blunt.asm.Assembler;
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
    public Instructions assemble(Assembler asm, Register target, Linkage linkage) {
        
        Instructions instructions = new Instructions();
        
        instructions.append(func.assemble(asm, Register.PROCEDURE, Linkage.NEXT));
        instructions.append(arg.assemble(asm, Register.ARG, Linkage.NEXT).preserving(Register.PROCEDURE));

        if (linkage == Linkage.RETURN && target == Register.VAL) {
            instructions.applyTail();

        } else {
            instructions.pushRegister(Register.ENV);
            instructions.apply();
            instructions.popRegister(Register.ENV);
            if (target != Register.VAL)
                instructions.copy(target, Register.VAL);

            instructions.finishWithLinkage(linkage);
        }

        return instructions;
    }

    @Override
    public String toString() {
        return "(" + func + " " + arg + ")";
    }
}
