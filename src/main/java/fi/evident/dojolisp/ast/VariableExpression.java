package fi.evident.dojolisp.ast;

import fi.evident.dojolisp.asm.Instructions;
import fi.evident.dojolisp.asm.Linkage;
import fi.evident.dojolisp.asm.Register;
import fi.evident.dojolisp.eval.VariableReference;
import fi.evident.dojolisp.types.Type;
import fi.evident.dojolisp.types.TypeEnvironment;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class VariableExpression extends Expression {
    
    private final VariableReference var;

    public VariableExpression(VariableReference var) {
        this.var = requireNonNull(var);
    }

    @Override
    public Type typeCheck(TypeEnvironment env) {
        return var.type;
    }

    @Override
    public void assemble(Instructions instructions, Register target, Linkage linkage) {
        instructions.loadVariable(target, var);
        instructions.finishWithLinkage(linkage);
    }
}
