package komu.blunt.asm.opcodes;

import komu.blunt.asm.Register;
import komu.blunt.asm.VM;

import static com.google.common.base.Preconditions.checkNotNull;

public class OpPushRegister extends OpCode {

    private final Register register;

    public OpPushRegister(Register register) {
        this.register = checkNotNull(register);
    }

    @Override
    public void execute(VM vm) {
        vm.push(vm.get(register));
    }

    @Override
    public boolean modifies(Register register) {
        return false;
    }

    @Override
    public String toString() {
        return String.format("(push %s)", register);
    }
}
