package komu.blunt.asm.opcodes;

import komu.blunt.asm.VM;

public abstract class OpCode {
    OpCode() { }

    public abstract void execute(VM vm);

    @Override
    public abstract String toString();
}
