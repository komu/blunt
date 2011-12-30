package komu.blunt.asm.opcodes;

import komu.blunt.asm.Register;
import komu.blunt.asm.VM;
import komu.blunt.eval.Environment;
import komu.blunt.objects.CompoundProcedure;
import komu.blunt.objects.Function;

public class OpApply extends OpCode {

    private final Register procedureRegister;
    private final Register argRegister;

    public OpApply(Register procedureRegister, Register argRegister) {
        this.procedureRegister = procedureRegister;
        this.argRegister = argRegister;
    }

    @Override
    public void execute(VM vm) {
        Object procedure = vm.get(procedureRegister);
        Object arg = vm.get(argRegister);

        if (procedure instanceof Function) {
            executePrimitive(vm, (Function) procedure, arg);
        } else {
            executeCompound(vm, (CompoundProcedure) procedure, arg);
        }
    }

    private void executePrimitive(VM vm, Function procedure, Object arg) {
        Object value = procedure.apply(arg);
        vm.set(Register.VAL, value);
    }

    private void executeCompound(VM vm, CompoundProcedure procedure, Object arg) {
        Environment env = procedure.env.extend(arg);
        vm.save(Register.ENV, Register.PC, Register.PROCEDURE, Register.ARG);
        vm.set(Register.ENV, env);
        vm.set(Register.PC, procedure.address);
    }

    @Override
    public String toString() {
        return String.format("(apply %s %s)", procedureRegister, argRegister);
    }
}
