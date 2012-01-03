package komu.blunt.asm.opcodes;

import komu.blunt.asm.Register;
import komu.blunt.asm.VM;
import komu.blunt.eval.Environment;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class OpCreateEnvironment extends OpCode {
    private final Register target;
    private final Register regSrc;
    private final Register regArg;
    private final int envSize;

    public OpCreateEnvironment(Register target, Register regSrc, Register regArg, int envSize) {
        checkArgument(envSize >= 0);

        this.target = checkNotNull(target);
        this.regSrc = checkNotNull(regSrc);
        this.regArg = checkNotNull(regArg);
        this.envSize = envSize;
    }

    @Override
    public void execute(VM vm) {
        Environment env = (Environment) vm.get(regSrc);
        Object arg = vm.get(regArg);
        
        Environment newEnv = env.extend(envSize, arg);
        vm.set(target, newEnv);
    }

    @Override
    public String toString() {
        return String.format("(load %s (create-env %s %s %d))", target, regSrc, regArg, envSize);
    }
}
