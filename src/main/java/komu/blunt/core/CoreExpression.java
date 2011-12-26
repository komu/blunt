package komu.blunt.core;

import komu.blunt.asm.Instructions;
import komu.blunt.asm.Linkage;
import komu.blunt.asm.Register;

public abstract class CoreExpression {
    public abstract void assemble(Instructions instructions, Register target, Linkage linkage);
}
