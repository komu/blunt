package komu.blunt.core;

import komu.blunt.asm.Assembler;
import komu.blunt.asm.Instructions;
import komu.blunt.asm.Linkage;
import komu.blunt.asm.Register;

public final class CoreEmptyExpression extends CoreExpression {

    public static final CoreExpression INSTANCE = new CoreEmptyExpression();

    private CoreEmptyExpression() { }
    
    @Override
    public Instructions assemble(Assembler asm, Register target, Linkage linkage) {
        Instructions instructions = new Instructions();
        instructions.finishWithLinkage(linkage);
        return instructions;
    }
}
