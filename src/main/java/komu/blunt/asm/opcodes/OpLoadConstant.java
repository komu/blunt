package komu.blunt.asm.opcodes;

import komu.blunt.asm.Register;
import komu.blunt.asm.VM;

import static com.google.common.base.Preconditions.checkNotNull;

public final class OpLoadConstant extends OpCode {
    private final Register register;
    private final Object value;

    public OpLoadConstant(Register register, Object value) {
        this.register = checkNotNull(register);
        this.value = value;
    }

    @Override
    public void execute(VM vm) {
        vm.set(register, value);
    }

    @Override
    public String toString() {
        return String.format("(load %s (constant %s))", register, value);
    }
}
