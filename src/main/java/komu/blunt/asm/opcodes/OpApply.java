package komu.blunt.asm.opcodes;

import komu.blunt.asm.Register;
import komu.blunt.asm.VM;
import komu.blunt.objects.CompoundProcedure;
import komu.blunt.objects.Function;

public final class OpApply extends OpCode {

    public static final OpApply INSTANCE = new OpApply();

    private OpApply() { }

    @Override
    public void execute(VM vm) {
        Object procedure = vm.procedure;

        if (procedure instanceof Function) {
            executePrimitive(vm, (Function) procedure);
        } else {
            executeCompound(vm, (CompoundProcedure) procedure);
        }
    }

    private void executePrimitive(VM vm, Function procedure) {
        vm.val = procedure.apply(vm.arg);
    }

    private void executeCompound(VM vm, CompoundProcedure procedure) {
        vm.push(vm.env);
        vm.push(vm.pc);

        vm.env = procedure.env;
        vm.pc = procedure.address;
    }

    @Override
    public String toString() {
        return String.format("(apply %s %s)", Register.PROCEDURE, Register.ARG);
    }
}
