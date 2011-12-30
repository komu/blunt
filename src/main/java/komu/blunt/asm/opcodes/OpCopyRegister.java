package komu.blunt.asm.opcodes;

import komu.blunt.asm.Register;
import komu.blunt.asm.VM;

import static com.google.common.base.Preconditions.checkNotNull;

public class OpCopyRegister extends OpCode {
    private final Register target;
    private final Register source;


    public OpCopyRegister(Register target, Register source) {
        this.target = checkNotNull(target);
        this.source = checkNotNull(source);
    }

    @Override
    public void execute(VM vm) {
        vm.set(target, vm.get(source));
    }

    @Override
    public String toString() {
        return String.format("(copy %s %s)", target, source);
    }
}
