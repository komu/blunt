package fi.evident.dojolisp.ast;

import fi.evident.dojolisp.asm.Instructions;
import fi.evident.dojolisp.asm.Linkage;
import fi.evident.dojolisp.asm.Register;
import fi.evident.dojolisp.types.Type;

public abstract class Expression {
    public abstract Type typeCheck();
    public abstract void assemble(Instructions instructions, Register target, Linkage linkage);
}
