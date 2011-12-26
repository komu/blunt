package komu.blunt.ast;

import komu.blunt.core.CoreExpression;
import komu.blunt.core.CoreIfExpression;
import komu.blunt.eval.RootBindings;
import komu.blunt.eval.StaticEnvironment;
import komu.blunt.types.*;
import komu.blunt.utils.ListUtils;

import static com.google.common.base.Preconditions.checkNotNull;

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
    public CoreExpression analyze(StaticEnvironment env, RootBindings rootBindings) {
        return new CoreIfExpression(test.analyze(env, rootBindings), consequent.analyze(env, rootBindings), alternative.analyze(env, rootBindings));
    }

    @Override
    public TypeCheckResult<Type> typeCheck(ClassEnv ce, TypeChecker tc, Assumptions as) {
        TypeCheckResult<Type> tyTest = test.typeCheck(ce, tc, as); 
        TypeCheckResult<Type> tyConsequent = consequent.typeCheck(ce, tc, as); 
        TypeCheckResult<Type> tyAlternative = alternative.typeCheck(ce, tc, as);
        
        tc.unify(tyTest.value, Type.BOOLEAN);
        tc.unify(tyConsequent.value, tyAlternative.value);

        return new TypeCheckResult<Type>(ListUtils.append(tyTest.predicates, tyConsequent.predicates, tyAlternative.predicates),
                tyConsequent.value);
    }

    @Override
    public String toString() {
        return "(if " + test + " " + consequent + " " + alternative + ")";
    }
}
