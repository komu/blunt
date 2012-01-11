package komu.blunt.asm.opcodes;

import komu.blunt.asm.Label;
import komu.blunt.asm.Register;
import komu.blunt.asm.VM;
import komu.blunt.eval.Environment;
import komu.blunt.objects.CompoundProcedure;

import static com.google.common.base.Preconditions.checkNotNull;

public final class OpLoadLambda extends OpCode {
    private final Register target;
    private final Label label;

    public OpLoadLambda(Register target, Label label) {
        this.target = checkNotNull(target);
        this.label = checkNotNull(label);
    }

    @Override
    public void execute(VM vm) {
        Environment environment = (Environment) vm.get(Register.ENV);
        CompoundProcedure procedure = new CompoundProcedure(label.getAddress(), environment);
        vm.set(target, procedure);
    }

    @Override
    public boolean modifies(Register register) {
        return register == target;
    }

    @Override
    public String toString() {
        return String.format("(load %s (lambda %s ENV))", target, label);
    }
}
