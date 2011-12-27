package komu.blunt.ast;

import static com.google.common.base.Preconditions.checkNotNull;
import static komu.blunt.types.Kind.STAR;
import static komu.blunt.types.Type.functionType;

import komu.blunt.core.CoreApplicationExpression;
import komu.blunt.core.CoreExpression;
import komu.blunt.eval.StaticEnvironment;
import komu.blunt.types.Assumptions;
import komu.blunt.types.ClassEnv;
import komu.blunt.types.Type;
import komu.blunt.types.TypeCheckResult;
import komu.blunt.types.TypeChecker;
import komu.blunt.utils.ListUtils;

public final class ASTApplication extends ASTExpression {

    public final ASTExpression func;
    public final ASTExpression arg;

    public ASTApplication(ASTExpression func, ASTExpression arg) {
        this.func = checkNotNull(func);
        this.arg = checkNotNull(arg);
    }

    @Override
    public CoreExpression analyze(StaticEnvironment env) {
        return new CoreApplicationExpression(func.analyze(env), arg.analyze(env));
    }

    @Override
    public TypeCheckResult<Type> typeCheck(ClassEnv ce, TypeChecker tc, Assumptions as) {
        TypeCheckResult<Type> te = func.typeCheck(ce, tc, as);
        TypeCheckResult<Type> tf = arg.typeCheck(ce, tc, as);

        Type t = tc.newTVar(STAR);

        tc.unify(functionType(tf.value, t), te.value);

        return new TypeCheckResult<Type>(ListUtils.append(te.predicates, tf.predicates), t);
    }

    @Override
    public String toString() {
        return "(" + func + " " + arg + ")";
    }
}
