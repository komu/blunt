package komu.blunt.asm.opcodes;

import komu.blunt.asm.Label;
import komu.blunt.asm.Register;
import komu.blunt.asm.VM;
import komu.blunt.objects.TypeConstructorValue;

import static com.google.common.base.Preconditions.checkNotNull;

public final class OpJumpIfFalse extends OpCode {

    private final Register register;
    private final Label label;

    public OpJumpIfFalse(Register register, Label label) {
        this.register = checkNotNull(register);
        this.label = checkNotNull(label);
    }

    @Override
    public void execute(VM vm) {
        Object value = vm.get(register);
        if (isFalse(value))
            vm.pc = label.getAddress();
    }

    @Override
    public boolean modifies(Register register) {
        return register == Register.PC;
    }

    public static boolean isFalse(Object value) {
        return Boolean.FALSE.equals(value)
            || (value instanceof TypeConstructorValue && ((TypeConstructorValue) value).name.equals("False"));
    }

    @Override
    public String toString() {
        return String.format("(jump-if-false %s %s)", register, label);
    }
}
