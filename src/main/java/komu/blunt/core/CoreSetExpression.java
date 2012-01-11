package komu.blunt.core;

import komu.blunt.analyzer.VariableReference;
import komu.blunt.asm.Assembler;
import komu.blunt.asm.Instructions;
import komu.blunt.asm.Linkage;
import komu.blunt.asm.Register;
import komu.blunt.stdlib.BasicValues;

public final class CoreSetExpression extends CoreExpression {

    private final VariableReference var;
    private final CoreExpression exp;

    public CoreSetExpression(VariableReference var, CoreExpression exp) {
        this.var = var;
        this.exp = exp;
    }

    @Override
    public Instructions assemble(Assembler asm, Register target, Linkage linkage) {
        Instructions instructions = new Instructions();

        instructions.pushRegister(Register.VAL); // TODO: only save var if needed

        instructions.append(exp.assemble(asm, Register.VAL, Linkage.NEXT));
        instructions.storeVariable(var, Register.VAL);

        instructions.popRegister(Register.VAL);

        instructions.loadConstant(target, BasicValues.UNIT);
        instructions.finishWithLinkage(linkage);

        return instructions;
    }

    @Override
    public String toString() {
        return "(set! " + var.name + " " + exp + ")";
    }
}
