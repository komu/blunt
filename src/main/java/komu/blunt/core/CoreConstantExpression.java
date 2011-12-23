package komu.blunt.core;

import komu.blunt.asm.Instructions;
import komu.blunt.asm.Linkage;
import komu.blunt.asm.Register;
import komu.blunt.types.Type;
import komu.blunt.types.TypeEnvironment;

import static com.google.common.base.Preconditions.checkNotNull;

public final class CoreConstantExpression extends CoreExpression {

    private final Object value;

    public CoreConstantExpression(Object value) {
        this.value = checkNotNull(value);
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
