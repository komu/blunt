package komu.blunt.asm.opcodes;

import komu.blunt.asm.Register;
import komu.blunt.asm.VM;

import static com.google.common.base.Preconditions.checkNotNull;

public class OpPopRegister extends OpCode {

    private final Register target;

    public OpPopRegister(Register target) {
        this.target = checkNotNull(target);
    }

    @Override
    public void execute(VM vm) {
        vm.set(target, vm.pop());
    }

    @Override
    public boolean modifies(Register register) {
        return register == target;
    }

    @Override
    public String toString() {
        return String.format("(pop %s)", target);
    }
}
