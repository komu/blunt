package komu.blunt.core;

import komu.blunt.asm.*;

import static com.google.common.base.Preconditions.checkNotNull;

public final class CoreIfExpression extends CoreExpression {

    private final CoreExpression condition;
    private final CoreExpression consequent;
    private final CoreExpression alternative;

    public CoreIfExpression(CoreExpression condition, CoreExpression consequent, CoreExpression alternative) {
        this.condition = checkNotNull(condition);
        this.consequent = checkNotNull(consequent);
        this.alternative = checkNotNull(alternative);
    }

    @Override
    public void assemble(Assembler asm, Instructions instructions, Register target, Linkage linkage) {
        Label after = asm.newLabel("if-after");
        Label falseBranch = asm.newLabel("if-false");

        Linkage trueLinkage = (linkage == Linkage.NEXT) ? Linkage.jump(after) : linkage;

        // Since the target register is safe to overwrite, we borrow it
        // for evaluating the condition as well.
        condition.assemble(asm, instructions, target, Linkage.NEXT);
        instructions.jumpIfFalse(target, falseBranch);

        consequent.assemble(asm, instructions, target, trueLinkage);
        instructions.label(falseBranch);
        alternative.assemble(asm, instructions, target, linkage);
        instructions.label(after);

        instructions.finishWithLinkage(linkage);
    }

    @Override
    public String toString() {
        return "(if " + condition + " " + consequent + " " + alternative + ")";
    }
}
