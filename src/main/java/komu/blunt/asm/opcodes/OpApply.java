package komu.blunt.asm.opcodes;

import komu.blunt.asm.Register;
import komu.blunt.asm.VM;
import komu.blunt.objects.CompoundProcedure;
import komu.blunt.objects.PrimitiveProcedure;
import komu.blunt.objects.Procedure;

public final class OpApply extends OpCode {

    public static final OpApply NORMAL = new OpApply(false);
    public static final OpApply TAIL = new OpApply(true);

    private final boolean tail;

    private OpApply(boolean tail) {
        this.tail = tail;
    }

    @Override
    public void execute(VM vm) {
        Procedure procedure = vm.procedure;

        if (procedure instanceof PrimitiveProcedure)
            executePrimitive(vm, (PrimitiveProcedure) procedure);
        else
            executeCompound(vm, (CompoundProcedure) procedure);
    }

    @Override
    public boolean modifies(Register register) {
        // Though the application itself will only modify PC, ENV or VAL, the called code could modify anything.
        return true;
    }

    private void executePrimitive(VM vm, PrimitiveProcedure procedure) {
        vm.val = procedure.apply(vm.arg);
        vm.pc = (Integer) vm.pop();
    }

    private void executeCompound(VM vm, CompoundProcedure procedure) {
        vm.env = procedure.env;
        vm.pc = procedure.address;
    }

    @Override
    public String toString() {
        return String.format("(%s %s %s)", tail ? "tail-call" : "call", Register.PROCEDURE, Register.ARG);
    }
}
