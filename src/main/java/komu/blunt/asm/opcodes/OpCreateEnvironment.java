package komu.blunt.asm.opcodes;

import komu.blunt.asm.Register;
import komu.blunt.asm.VM;
import komu.blunt.eval.Environment;

import static com.google.common.base.Preconditions.checkArgument;

public final class OpCreateEnvironment extends OpCode {

    private final int envSize;

    public OpCreateEnvironment(int envSize) {
        checkArgument(envSize >= 0);

        this.envSize = envSize;
    }

    @Override
    public void execute(VM vm) {
        Environment env = (Environment) vm.env;
        vm.env = env.extend(envSize, vm.arg);
    }

    @Override
    public String toString() {
        return String.format("(load %s (create-env %s %s %d))", Register.ENV, Register.ENV, Register.ARG, envSize);
    }
}
