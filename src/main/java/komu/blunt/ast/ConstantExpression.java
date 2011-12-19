package komu.blunt.ast;

import komu.blunt.asm.Instructions;
import komu.blunt.asm.Linkage;
import komu.blunt.asm.Register;
import komu.blunt.types.Type;
import komu.blunt.types.TypeEnvironment;

import static komu.blunt.utils.Objects.requireNonNull;

public final class ConstantExpression extends Expression {

    private final Object value;

    public ConstantExpression(Object value) {
        this.value = requireNonNull(value);
    }

    @Override
    public Type typeCheck(TypeEnvironment env) {
        return (value == null) ? Type.UNIT : Type.fromClass(value.getClass());
    }

    @Override
    public void assemble(Instructions instructions, Register target, Linkage linkage) {
        instructions.loadConstant(target, value);
        instructions.finishWithLinkage(linkage);
    }
}
