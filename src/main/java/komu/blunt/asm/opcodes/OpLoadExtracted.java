package komu.blunt.asm.opcodes;

import komu.blunt.asm.Register;
import komu.blunt.asm.VM;
import komu.blunt.core.PatternPath;
import komu.blunt.objects.TypeConstructorValue;

import static com.google.common.base.Preconditions.checkNotNull;

public class OpLoadExtracted extends OpCode {

    private Register target;
    private Register source;
    private PatternPath path;

    public OpLoadExtracted(Register target, Register source, PatternPath path) {
        this.target = checkNotNull(target);
        this.source = checkNotNull(source);
        this.path = checkNotNull(path);
    }

    @Override
    public void execute(VM vm) {
        Object object = vm.get(source);
        for (int index : path.indices()) {
            object = ((TypeConstructorValue) object).items[index];
        }
        vm.set(target, object);
    }

    @Override
    public boolean modifies(Register register) {
        return register == target;
    }

    @Override
    public String toString() {
        return String.format("(load %s (extract %s %s))", target, source, path);
    }
}
