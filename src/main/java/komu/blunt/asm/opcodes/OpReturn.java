package komu.blunt.asm.opcodes;

import komu.blunt.asm.VM;
import komu.blunt.eval.Environment;

public final class OpReturn extends OpCode {

    public static final OpReturn INSTANCE = new OpReturn();

    private OpReturn() { }

    @Override
    public String toString() {
        return "(return)";
    }

    @Override
    public void execute(VM vm) {
        vm.pc = (Integer) vm.pop();
        vm.env = (Environment) vm.pop();
    }
}
