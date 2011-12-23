package komu.blunt.core;

import komu.blunt.asm.Instructions;
import komu.blunt.asm.Label;
import komu.blunt.asm.Linkage;
import komu.blunt.asm.Register;
import komu.blunt.objects.Symbol;
import komu.blunt.types.*;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public final class CoreLambdaExpression extends CoreExpression {

    private final List<Symbol> argumentNames;
    private final CoreExpression body;

    public CoreLambdaExpression(List<Symbol> argumentNames, CoreExpression body) {
        this.argumentNames = new ArrayList<Symbol>(argumentNames);
        this.body = checkNotNull(body);
    }

    @Override
    public Type typeCheck(TypeEnvironment env) {
        TypeEnvironment bodyEnv = new TypeEnvironment(env);

        List<Type> argumentTypes = new ArrayList<Type>(argumentNames.size());
        for (Symbol symbol : argumentNames) {
            TypeVariable var = env.newVar(Kind.STAR);
            env.bind(symbol, new TypeScheme(var));
            argumentTypes.add(var);
        }

        return Type.makeFunctionType(argumentTypes, body.typeCheck(bodyEnv));
    }

    @Override
    public void assemble(Instructions instructions, Register target, Linkage linkage) {
        // TODO: place the lambda in a new code section
        Label lambda = instructions.newLabel("lambda");
        Label afterLambda = instructions.newLabel("after-lambda");

        instructions.loadLambda(target, lambda);
        if (linkage == Linkage.NEXT) {
            instructions.jump(afterLambda);
        } else {
            instructions.finishWithLinkage(linkage);
        }

        instructions.label(lambda);
        body.assemble(instructions, Register.VAL, Linkage.RETURN);
        instructions.label(afterLambda);
    }
}
