package komu.blunt.core;

import komu.blunt.asm.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static komu.blunt.asm.opcodes.OpJumpIfFalse.isFalse;

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
    public Instructions assemble(Assembler asm, Register target, Linkage linkage) {
        Instructions instructions = new Instructions();

        Label after = asm.newLabel("if-after");
        Label falseBranch = asm.newLabel("if-false");

        Linkage trueLinkage = (linkage == Linkage.NEXT) ? Linkage.jump(after) : linkage;

        // Since the target register is safe to overwrite, we borrow it
        // for evaluating the condition as well.
        instructions.append(condition.assemble(asm, target, Linkage.NEXT));
        instructions.jumpIfFalse(target, falseBranch);

        instructions.append(consequent.assemble(asm, target, trueLinkage));
        instructions.label(falseBranch);
        instructions.append(alternative.assemble(asm, target, linkage));
        instructions.label(after);

        instructions.finishWithLinkage(linkage);
        
        return instructions;
    }

    @Override
    public CoreExpression simplify() {
        CoreExpression simplifiedCondition = condition.simplify();
        CoreExpression simplifiedConsequent = consequent.simplify();
        CoreExpression simplifiedAlternative = alternative.simplify();

        if (simplifiedCondition instanceof CoreConstantExpression) {
            CoreConstantExpression constant = (CoreConstantExpression) simplifiedCondition;
            return isFalse(constant) ? simplifiedAlternative : simplifiedConsequent;
        } else {
            return new CoreIfExpression(simplifiedCondition, simplifiedConsequent, simplifiedAlternative);
        }
    }

    @Override
    public String toString() {
        return "(if " + condition + " " + consequent + " " + alternative + ")";
    }
}
