package komu.blunt.asm.opcodes;

import komu.blunt.asm.Register;
import komu.blunt.asm.VM;
import komu.blunt.objects.TypeConstructorValue;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class OpLoadConstructed extends OpCode {

    private final Register target;
    private final int index;
    private final String name;
    private final int size;

    public OpLoadConstructed(Register target, int index, String name, int size) {
        checkArgument(size >= 0);

        this.target = checkNotNull(target);
        this.index = checkNotNull(index);
        this.name = checkNotNull(name);
        this.size = size;
    }

    @Override
    public void execute(VM vm) {
        Object[] array = new Object[size];
        for (int i = 0; i < size; i++)
            array[i] = vm.pop();
        vm.set(target, new TypeConstructorValue(index, name, array));
    }

    @Override
    public String toString() {
        return String.format("(load %s (%s %d))", target, name, size);
    }
}
