package komu.blunt.asm.opcodes;

import komu.blunt.asm.Label;
import komu.blunt.asm.VM;

import static com.google.common.base.Preconditions.checkNotNull;

public final class OpJump extends OpCode {
    private final Label label;

    public OpJump(Label label) {
        this.label = checkNotNull(label);
    }

    @Override
    public void execute(VM vm) {
        vm.jump(label);
    }

    @Override
    public String toString() {
        return String.format("(jump %s)", label);
    }
}
