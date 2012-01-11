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

        instructions.append(exp.assemble(asm, Register.VAL, Linkage.NEXT));
        instructions.storeVariable(var, Register.VAL);

        instructions.finishWithLinkage(linkage);

        return instructions;
    }

    @Override
    public CoreExpression simplify() {
        return new CoreSetExpression(var, exp.simplify());
    }

    @Override
    public String toString() {
        return "(set! " + var.name + " " + exp + ")";
    }
}
