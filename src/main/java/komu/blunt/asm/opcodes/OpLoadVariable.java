package komu.blunt.asm.opcodes;

import komu.blunt.analyzer.VariableReference;
import komu.blunt.asm.Register;
import komu.blunt.asm.VM;
import komu.blunt.eval.Environment;

import static com.google.common.base.Preconditions.checkNotNull;

public final class OpLoadVariable extends OpCode {
    private final Register target;
    private final VariableReference variable;

    public OpLoadVariable(Register target, VariableReference variable) {
        this.target = checkNotNull(target);
        this.variable = checkNotNull(variable);
    }

    @Override
    public void execute(VM vm) {
        Environment env = variable.isGlobal() ? vm.getGlobalEnvironment() : vm.env;
        Object value = env.lookup(variable);
        vm.set(target, value);
    }

    @Override
    public boolean modifies(Register register) {
        return register == target;
    }

    @Override
    public String toString() {
        return String.format("(load %s (variable %d %d)) ; %s", target, variable.frame, variable.offset, variable.name);
    }
}
