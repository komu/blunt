package komu.blunt.ast;

import static com.google.common.base.Preconditions.checkNotNull;
import static komu.blunt.types.Kind.STAR;
import static komu.blunt.types.Type.functionType;

import komu.blunt.core.CoreApplicationExpression;
import komu.blunt.core.CoreExpression;
import komu.blunt.eval.StaticEnvironment;
import komu.blunt.types.Type;
import komu.blunt.types.TypeCheckResult;
import komu.blunt.types.TypeCheckingContext;
import komu.blunt.utils.CollectionUtils;

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
    public TypeCheckResult<Type> typeCheck(final TypeCheckingContext ctx) {
        TypeCheckResult<Type> te = func.typeCheck(ctx);
        TypeCheckResult<Type> tf = arg.typeCheck(ctx);

        Type t = ctx.tc.newTVar(STAR);

        ctx.tc.unify(functionType(tf.value, t), te.value);

        return new TypeCheckResult<Type>(CollectionUtils.append(te.predicates, tf.predicates), t);
    }

    @Override
    public String toString() {
        return "(" + func + " " + arg + ")";
    }
}
