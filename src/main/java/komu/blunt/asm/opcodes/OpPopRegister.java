package komu.blunt.asm.opcodes;

import komu.blunt.asm.Register;
import komu.blunt.asm.VM;

import static com.google.common.base.Preconditions.checkNotNull;

public class OpPopRegister extends OpCode {

    private final Register register;

    public OpPopRegister(Register register) {
        this.register = checkNotNull(register);
    }

    @Override
    public void execute(VM vm) {
        vm.set(register, vm.pop());
    }

    @Override
    public String toString() {
        return String.format("(pop %s)", register);
    }
}
