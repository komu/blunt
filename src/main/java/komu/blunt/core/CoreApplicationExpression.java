package komu.blunt.core;

import komu.blunt.asm.Instructions;
import komu.blunt.asm.Linkage;
import komu.blunt.asm.Register;
import komu.blunt.types.Kind;
import komu.blunt.types.Type;
import komu.blunt.types.TypeEnvironment;

import static com.google.common.base.Preconditions.checkNotNull;

public final class CoreApplicationExpression extends CoreExpression {
    
    private final CoreExpression func;
    private final CoreExpression arg;

    public CoreApplicationExpression(CoreExpression func, CoreExpression arg) {
        this.func = checkNotNull(func);
        this.arg = checkNotNull(arg);
    }

    @Override
    public Type typeCheck(TypeEnvironment env) {
        Type argType = arg.typeCheck(env);
        Type returnType = env.newVar(Kind.STAR);
        Type ty = Type.makeFunctionType(argType, returnType);

        env.unify(func.typeCheck(env), ty);

        return returnType;
    }
    
    @Override
    public void assemble(Instructions instructions, Register target, Linkage linkage) {
        func.assemble(instructions, Register.PROCEDURE, Linkage.NEXT);
        instructions.pushRegister(Register.PROCEDURE);

        arg.assemble(instructions, Register.ARG, Linkage.NEXT);

        instructions.popRegister(Register.PROCEDURE);

        // TODO: tail calls

        instructions.apply(Register.PROCEDURE, Register.ARG);
        if (target != Register.VAL)
            instructions.copy(target, Register.VAL);

        instructions.finishWithLinkage(linkage);
    }
}
