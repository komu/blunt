package komu.blunt.asm.opcodes;

import com.google.common.base.Objects;
import komu.blunt.asm.Register;
import komu.blunt.asm.VM;

import static com.google.common.base.Preconditions.checkNotNull;
import static komu.blunt.stdlib.BasicValues.booleanToConstructor;

public class OpEqualConstant extends OpCode {

    private final Register target;
    private final Register source;
    private final Object value;

    public OpEqualConstant(Register target, Register source, Object value) {
        this.target = checkNotNull(target);
        this.source = checkNotNull(source);
        this.value = checkNotNull(value);
    }

    @Override
    public void execute(VM vm) {
        Object val = vm.get(source);

        boolean result = Objects.equal(val, value);
        vm.set(target, booleanToConstructor(result));
    }

    @Override
    public boolean modifies(Register register) {
        return register == target;
    }

    @Override
    public String toString() {
        return String.format("(load %s (= %s %s))", target, source, value);
    }
}
