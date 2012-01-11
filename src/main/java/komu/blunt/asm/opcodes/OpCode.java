package komu.blunt.asm.opcodes;

import komu.blunt.asm.Register;
import komu.blunt.asm.VM;

public abstract class OpCode {
    OpCode() { }

    public abstract void execute(VM vm);
    public abstract boolean modifies(Register register);

    @Override
    public abstract String toString();
}
