package fi.evident.dojolisp.ast;

import fi.evident.dojolisp.asm.Instructions;
import fi.evident.dojolisp.asm.Linkage;
import fi.evident.dojolisp.asm.Register;
import fi.evident.dojolisp.types.Type;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class ConstantExpression extends Expression {

    private final Object value;

    public ConstantExpression(Object value) {
        this.value = requireNonNull(value);
    }

    @Override
    public Type typeCheck() {
        return (value == null) ? Type.UNIT : Type.fromClass(value.getClass());
    }

    @Override
    public void assemble(Instructions instructions, Register target, Linkage linkage) {
        instructions.loadConstant(target, value);
        instructions.finishWithLinkage(linkage);
    }
}
