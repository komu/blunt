package komu.blunt.asm.opcodes;

import komu.blunt.analyzer.VariableReference;
import komu.blunt.asm.Register;
import komu.blunt.asm.VM;
import komu.blunt.eval.Environment;

import static com.google.common.base.Preconditions.checkNotNull;

public final class OpLoadVariable extends OpCode {
    private final Register register;
    private final VariableReference variable;

    public OpLoadVariable(Register register, VariableReference variable) {
        this.register = checkNotNull(register);
        this.variable = checkNotNull(variable);
    }

    @Override
    public void execute(VM vm) {
        Environment env = variable.isGlobal() ? vm.getGlobalEnvironment() : (Environment) vm.get(Register.ENV);
        Object value = env.lookup(variable);
        vm.set(register, value);
    }

    @Override
    public String toString() {
        return String.format("(load %s (variable %d %d)) ; %s", register, variable.frame, variable.offset, variable.name);
    }
}
