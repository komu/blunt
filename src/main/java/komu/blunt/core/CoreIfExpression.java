package komu.blunt.core;

import komu.blunt.asm.Instructions;
import komu.blunt.asm.Label;
import komu.blunt.asm.Linkage;
import komu.blunt.asm.Register;
import komu.blunt.types.Type;
import komu.blunt.types.TypeEnvironment;

import static komu.blunt.utils.Objects.requireNonNull;

public final class CoreIfExpression extends CoreExpression {

    private final CoreExpression condition;
    private final CoreExpression consequent;
    private final CoreExpression alternative;

    public CoreIfExpression(CoreExpression condition, CoreExpression consequent, CoreExpression alternative) {
        this.condition = requireNonNull(condition);
        this.consequent = requireNonNull(consequent);
        this.alternative = requireNonNull(alternative);
    }

    @Override
    public void assemble(Instructions instructions, Register target, Linkage linkage) {
        Label after = instructions.newLabel("if-after");
        Label falseBranch = instructions.newLabel("if-false");

        Linkage trueLinkage = (linkage == Linkage.NEXT) ? Linkage.jump(after) : linkage;

        // Since the target register is safe to overwrite, we borrow it
        // for evaluating the condition as well.
        condition.assemble(instructions, target, Linkage.NEXT);
        instructions.jumpIfFalse(target, falseBranch);

        consequent.assemble(instructions, target, trueLinkage);
        instructions.label(falseBranch);
        alternative.assemble(instructions, target, linkage);
        instructions.label(after);

        instructions.finishWithLinkage(linkage);
    }

    @Override
    public Type typeCheck(TypeEnvironment env) {
        Type conditionType = condition.typeCheck(env);
        
        env.assign(Type.BOOLEAN, conditionType);

        Type consequentType = consequent.typeCheck(env);
        Type alternativeType = alternative.typeCheck(env);

        env.unify(consequentType, alternativeType);

        return consequentType;
    }
}
