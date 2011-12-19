package komu.blunt.ast;

import komu.blunt.asm.Instructions;
import komu.blunt.asm.Linkage;
import komu.blunt.asm.Register;
import komu.blunt.eval.VariableReference;
import komu.blunt.objects.Symbol;
import komu.blunt.objects.Unit;
import komu.blunt.types.Kind;
import komu.blunt.types.Type;
import komu.blunt.types.TypeEnvironment;
import komu.blunt.types.TypeVariable;

import static komu.blunt.utils.Objects.requireNonNull;

public final class DefineExpression extends Expression {

    private final Symbol name;
    private final Expression expression;
    private final VariableReference var;

    public DefineExpression(Symbol name, Expression expression, VariableReference var) {
        this.name = requireNonNull(name);
        this.expression = requireNonNull(expression);
        this.var = requireNonNull(var);
    }

    @Override
    public Type typeCheck(TypeEnvironment env) {
        TypeEnvironment newEnv = new TypeEnvironment(env);

        TypeVariable type = env.newVar(Kind.STAR);
        newEnv.bind(name, type.quantifyAll());
        
        Type varType = newEnv.typeCheck(expression);

        env.bind(name, varType.quantifyAll());

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
