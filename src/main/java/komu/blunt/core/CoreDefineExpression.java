package komu.blunt.core;

import komu.blunt.asm.Instructions;
import komu.blunt.asm.Linkage;
import komu.blunt.asm.Register;
import komu.blunt.eval.RootBindings;
import komu.blunt.eval.VariableReference;
import komu.blunt.objects.Symbol;
import komu.blunt.objects.Unit;
import komu.blunt.types.Kind;
import komu.blunt.types.Type;
import komu.blunt.types.TypeEnvironment;
import komu.blunt.types.TypeVariable;

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
    public Type typeCheck(TypeEnvironment env) {
        TypeEnvironment newEnv = new TypeEnvironment(env);

        TypeVariable type = env.newVar(Kind.STAR);
        newEnv.bind(name, type.quantifyAll());
        
        Type varType = newEnv.typeCheck(expression);

        rootBindings.defineVariableType(name, varType.quantifyAll());

        return Type.UNIT;
    }

    @Override
    public void assemble(Instructions instructions, Register target, Linkage linkage) {
        expression.assemble(instructions, target, Linkage.NEXT);

        instructions.storeVariable(var, target);

        instructions.loadConstant(target, Unit.INSTANCE);
        instructions.finishWithLinkage(linkage);
    }
}
