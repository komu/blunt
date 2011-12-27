package komu.blunt.ast;

import komu.blunt.core.CoreExpression;
import komu.blunt.core.CoreIfExpression;
import komu.blunt.eval.StaticEnvironment;
import komu.blunt.types.Predicate;
import komu.blunt.types.Type;
import komu.blunt.types.TypeCheckResult;
import komu.blunt.types.TypeCheckingContext;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static komu.blunt.utils.CollectionUtils.append;

public final class ASTIf extends ASTExpression {
    public final ASTExpression test;
    public final ASTExpression consequent;
    public final ASTExpression alternative;

    public ASTIf(ASTExpression test, ASTExpression consequent, ASTExpression alternative) {
        this.test = checkNotNull(test);
        this.consequent = checkNotNull(consequent);
        this.alternative = checkNotNull(alternative);
    }

    @Override
    public <R, C> R accept(ASTVisitor<C, R> visitor, C ctx) {
        return visitor.visit(this, ctx);
    }

    @Override
    public CoreExpression analyze(StaticEnvironment env) {
        return new CoreIfExpression(test.analyze(env), consequent.analyze(env), alternative.analyze(env));
    }

    @Override
    public TypeCheckResult<Type> typeCheck(final TypeCheckingContext ctx) {
        TypeCheckResult<Type> tyTest = test.typeCheck(ctx);
        TypeCheckResult<Type> tyConsequent = consequent.typeCheck(ctx);
        TypeCheckResult<Type> tyAlternative = alternative.typeCheck(ctx);
        
        ctx.unify(tyTest.value, Type.BOOLEAN);
        ctx.unify(tyConsequent.value, tyAlternative.value);

        List<Predicate> predicates = append(tyTest.predicates, tyConsequent.predicates, tyAlternative.predicates);
        return new TypeCheckResult<Type>(predicates, tyConsequent.value);
    }

    @Override
    public String toString() {
        return "(if " + test + " " + consequent + " " + alternative + ")";
    }
}
