package komu.blunt.asm.opcodes;

import komu.blunt.analyzer.VariableReference;
import komu.blunt.asm.Register;
import komu.blunt.asm.VM;
import komu.blunt.eval.Environment;

import static com.google.common.base.Preconditions.checkNotNull;

public final class OpStoreVariable extends OpCode {
    private final VariableReference variable;
    private final Register register;

    public OpStoreVariable(VariableReference variable, Register register) {
        this.variable = checkNotNull(variable);
        this.register = checkNotNull(register);
    }

    @Override
    public void execute(VM vm) {
        Environment env = variable.isGlobal() ? vm.getGlobalEnvironment() : (Environment) vm.get(Register.ENV);
        Object value = vm.get(register);
        env.set(variable, value);
    }

    @Override
    public String toString() {
        return String.format("(store (variable %d %d) %s) ; %s", variable.frame, variable.offset, register, variable.name);
    }
}
