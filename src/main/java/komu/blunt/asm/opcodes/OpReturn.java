package komu.blunt.asm.opcodes;

import komu.blunt.asm.Register;
import komu.blunt.asm.VM;

public final class OpReturn extends OpCode {
    @Override
    public String toString() {
        return "(return)";
    }

    @Override
    public void execute(VM vm) {
        vm.restore(Register.ENV, Register.PC, Register.PROCEDURE, Register.ARG);
    }
}
