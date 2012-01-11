package komu.blunt.core;

import komu.blunt.analyzer.VariableReference;
import komu.blunt.asm.Assembler;
import komu.blunt.asm.Instructions;
import komu.blunt.asm.Linkage;
import komu.blunt.asm.Register;

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

        instructions.append(exp.assemble(asm, target, Linkage.NEXT));
        instructions.storeVariable(var, target);

        instructions.finishWithLinkage(linkage);

        return instructions;
    }

    @Override
    public String toString() {
        return "(set! " + var.name + " " + exp + ")";
    }
}
