package komu.blunt.ast;

import komu.blunt.asm.Instructions;
import komu.blunt.asm.Linkage;
import komu.blunt.asm.Register;
import komu.blunt.types.Type;
import komu.blunt.types.TypeEnvironment;

public abstract class Expression {
    public abstract Type typeCheck(TypeEnvironment env);
    public abstract void assemble(Instructions instructions, Register target, Linkage linkage);
}
