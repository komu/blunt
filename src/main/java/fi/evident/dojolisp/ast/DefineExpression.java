package fi.evident.dojolisp.ast;

import fi.evident.dojolisp.asm.Instructions;
import fi.evident.dojolisp.asm.Linkage;
import fi.evident.dojolisp.asm.Register;
import fi.evident.dojolisp.eval.VariableReference;
import fi.evident.dojolisp.objects.Symbol;
import fi.evident.dojolisp.objects.Unit;
import fi.evident.dojolisp.types.Kind;
import fi.evident.dojolisp.types.Type;
import fi.evident.dojolisp.types.TypeEnvironment;
import fi.evident.dojolisp.types.TypeVariable;

import java.util.Collections;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

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
        TypeVariable type = env.newVar(Kind.STAR);
        env.bind(name, type.quantify(Collections.<TypeVariable>emptySet()));

        Type varType = expression.typeCheck(env);

        env.unify(type, varType);

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
