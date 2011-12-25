package komu.blunt.ast;

import komu.blunt.core.CoreDefineExpression;
import komu.blunt.core.CoreExpression;
import komu.blunt.eval.RootBindings;
import komu.blunt.eval.StaticEnvironment;
import komu.blunt.eval.VariableReference;
import komu.blunt.objects.Symbol;
import komu.blunt.types.Kind;
import komu.blunt.types.Type;
import komu.blunt.types.TypeEnvironment;
import komu.blunt.types.TypeVariable;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ASTDefine {

    public final Symbol name;
    public final ASTExpression value;

    public ASTDefine(Symbol name, ASTExpression value) {
        this.name = checkNotNull(name);
        this.value = checkNotNull(value);
    }

    public CoreExpression analyze(StaticEnvironment env, RootBindings rootBindings) {
        VariableReference var = rootBindings.staticEnvironment.define(name);
        return new CoreDefineExpression(name, value.analyze(env, rootBindings), var, rootBindings);
    }

    public void typeCheck(RootBindings rootBindings) {
        TypeEnvironment env = rootBindings.createTypeEnvironment();
        TypeEnvironment newEnv = new TypeEnvironment(env);

        TypeVariable type = env.newVar(Kind.STAR);
        newEnv.bind(name, type.quantifyAll());

        Type varType = newEnv.typeCheck(value);

        // TODO: define value in root
        rootBindings.defineVariableType(name, varType.quantifyAll());
    }

    @Override
    public String toString() {
        return "(define " + name + " " + value + ")";
    }
}
