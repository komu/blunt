package fi.evident.dojolisp.ast;

import fi.evident.dojolisp.asm.Instructions;
import fi.evident.dojolisp.asm.Linkage;
import fi.evident.dojolisp.asm.Register;
import fi.evident.dojolisp.types.Type;
import fi.evident.dojolisp.types.TypeEnvironment;

public abstract class Expression {
    public abstract Type typeCheck(TypeEnvironment env);
    public abstract void assemble(Instructions instructions, Register target, Linkage linkage);
}
