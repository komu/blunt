package komu.blunt.asm.opcodes;

import komu.blunt.asm.Register;
import komu.blunt.asm.VM;

import static com.google.common.base.Preconditions.checkNotNull;

public final class OpLoadConstant extends OpCode {
    private final Register target;
    private final Object value;

    public OpLoadConstant(Register target, Object value) {
        if (!target.isValidValue(value))
            throw new IllegalArgumentException("invalid value for register " + target + ": " + value);

        this.target = checkNotNull(target);
        this.value = value;
    }

    @Override
    public void execute(VM vm) {
        vm.set(target, value);
    }

    @Override
    public boolean modifies(Register register) {
        return register == target;
    }

    @Override
    public String toString() {
        return String.format("(load %s (constant %s))", target, value);
    }
}
