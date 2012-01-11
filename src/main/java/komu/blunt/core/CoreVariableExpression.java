package komu.blunt.core;

import komu.blunt.analyzer.VariableReference;
import komu.blunt.asm.Assembler;
import komu.blunt.asm.Instructions;
import komu.blunt.asm.Linkage;
import komu.blunt.asm.Register;

import static com.google.common.base.Preconditions.checkNotNull;

public final class CoreVariableExpression extends CoreExpression {
    
    private final VariableReference var;

    public CoreVariableExpression(VariableReference var) {
        this.var = checkNotNull(var);
    }

    @Override
    public Instructions assemble(Assembler asm, Register target, Linkage linkage) {
        Instructions instructions = new Instructions();
        instructions.loadVariable(target, var);
        instructions.finishWithLinkage(linkage);
        return instructions;
    }

    @Override
    public CoreExpression simplify() {
        return this;
    }

    @Override
    public String toString() {
        return var.name.toString();
    }
}
