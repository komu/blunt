package komu.blunt.core;

import komu.blunt.asm.Instructions;
import komu.blunt.asm.Linkage;
import komu.blunt.asm.Register;
import komu.blunt.eval.RootBindings;
import komu.blunt.eval.VariableReference;
import komu.blunt.objects.Symbol;
import komu.blunt.objects.Unit;

import static com.google.common.base.Preconditions.checkNotNull;

public final class CoreDefineExpression extends CoreExpression {

    private final Symbol name;
    private final CoreExpression expression;
    private final RootBindings rootBindings;
    private VariableReference var;

    public CoreDefineExpression(Symbol name, CoreExpression expression, VariableReference var, RootBindings rootBindings) {
        this.name = checkNotNull(name);
        this.expression = checkNotNull(expression);
        this.var = checkNotNull(var);
        this.rootBindings = checkNotNull(rootBindings);
    }

    @Override
    public void assemble(Instructions instructions, Register target, Linkage linkage) {
        expression.assemble(instructions, target, Linkage.NEXT);

        instructions.storeVariable(var, target);

        instructions.loadConstant(target, Unit.INSTANCE);
        instructions.finishWithLinkage(linkage);
    }
}
