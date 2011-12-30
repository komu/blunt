package komu.blunt.asm.opcodes;

import komu.blunt.asm.Register;
import komu.blunt.asm.VM;

import static com.google.common.base.Preconditions.checkNotNull;

public class OpLoadNewArray extends OpCode {
    private final Register target;
    private final int size;

    public OpLoadNewArray(Register target, int size) {
        this.target = checkNotNull(target);
        this.size = size;
    }

    @Override
    public void execute(VM vm) {
        Object[] value = new Object[size];
        vm.set(target, value);
    }

    @Override
    public String toString() {
        return String.format("(load %s (array %d))", target, size);
    }
}
