package fi.evident.dojolisp.ast;

import fi.evident.dojolisp.asm.Instructions;
import fi.evident.dojolisp.asm.Label;
import fi.evident.dojolisp.asm.Linkage;
import fi.evident.dojolisp.asm.Register;
import fi.evident.dojolisp.types.Type;
import fi.evident.dojolisp.types.TypeEnvironment;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class IfExpression extends Expression {

    private final Expression condition;
    private final Expression consequent;
    private final Expression alternative;

    public IfExpression(Expression condition, Expression consequent, Expression alternative) {
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
