package komu.blunt.asm.opcodes;

import komu.blunt.asm.Register;
import komu.blunt.asm.VM;
import komu.blunt.core.PatternPath;
import komu.blunt.objects.TypeConstructorValue;

import static com.google.common.base.Preconditions.checkNotNull;

public class OpLoadTag extends OpCode {

    private final Register target;
    private final Register source;
    private final PatternPath path;

    public OpLoadTag(Register target, Register source, PatternPath path) {
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
        vm.set(target, ((TypeConstructorValue) object).name);
    }

    @Override
    public String toString() {
        return String.format("(load %s (tag %s %s))", target, source, path);
    }
}
