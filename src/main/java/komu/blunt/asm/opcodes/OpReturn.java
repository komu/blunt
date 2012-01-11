package komu.blunt.asm.opcodes;

import komu.blunt.asm.VM;

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
        vm.env = vm.pop();
    }
}
