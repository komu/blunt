package komu.blunt.core;

import komu.blunt.asm.Instructions;
import komu.blunt.asm.Linkage;
import komu.blunt.asm.Register;
import komu.blunt.types.Kind;
import komu.blunt.types.Type;
import komu.blunt.types.TypeEnvironment;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public final class CoreApplicationExpression extends CoreExpression {
    
    private final CoreExpression func;
    private final List<CoreExpression> args;

    public CoreApplicationExpression(CoreExpression func, List<CoreExpression> args) {
        this.func = checkNotNull(func);
        this.args = checkNotNull(args);
    }

    @Override
    public Type typeCheck(TypeEnvironment env) {
        List<Type> argTypes = typeCheckArgs(env);

        Type returnType = env.newVar(Kind.STAR);

        Type ty;
        if (argTypes.size() == 1) 
            ty = Type.makeFunctionType(argTypes.get(0), returnType);
        else
            ty = Type.makeFunctionTypeOld(argTypes, returnType);
        
        env.unify(func.typeCheck(env), ty);

        return returnType;
    }
    
    private List<Type> typeCheckArgs(TypeEnvironment env) {
        List<Type> types = new ArrayList<Type>(args.size());
        
        for (CoreExpression arg : args)
            types.add(arg.typeCheck(env));
        
        return types;
    }

    @Override
    public void assemble(Instructions instructions, Register target, Linkage linkage) {
        // TODO: preserve registers only if needed

        func.assemble(instructions, Register.PROCEDURE, Linkage.NEXT);
        instructions.pushRegister(Register.PROCEDURE);

        instructions.loadNewArray(Register.ARGV, args.size());
        for (int i = 0; i < args.size(); i++) {
            instructions.pushRegister(Register.ARGV);
            instructions.pushRegister(Register.ENV);
            args.get(i).assemble(instructions, Register.VAL, Linkage.NEXT);
            instructions.popRegister(Register.ENV);
            instructions.popRegister(Register.ARGV);
            instructions.arrayStore(Register.ARGV, i, Register.VAL);
        }

        instructions.popRegister(Register.PROCEDURE);

        // TODO: tail calls

        instructions.apply(Register.PROCEDURE, Register.ARGV);
        if (target != Register.VAL)
            instructions.copy(target, Register.VAL);

        instructions.finishWithLinkage(linkage);
    }
}
