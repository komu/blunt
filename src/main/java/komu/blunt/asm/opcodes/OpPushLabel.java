package komu.blunt.asm.opcodes;

import komu.blunt.asm.Label;
import komu.blunt.asm.Register;
import komu.blunt.asm.VM;

import static com.google.common.base.Preconditions.checkNotNull;

public final class OpPushLabel extends OpCode {
    
    private final Label label;

    public OpPushLabel(Label label) {
        this.label = checkNotNull(label);
    }

    @Override
    public void execute(VM vm) {
        vm.push(label.getAddress());
    }

    @Override
    public boolean modifies(Register register) {
        return false;
    }

    @Override
    public String toString() {
        return String.format("(push (label %s))", label);
    }
}
