package komu.blunt.ast;

import static com.google.common.base.Preconditions.checkNotNull;

import komu.blunt.core.CoreExpression;
import komu.blunt.core.CoreIfExpression;
import komu.blunt.eval.StaticEnvironment;
import komu.blunt.types.Assumptions;
import komu.blunt.types.ClassEnv;
import komu.blunt.types.Type;
import komu.blunt.types.TypeCheckResult;
import komu.blunt.types.TypeChecker;
import komu.blunt.utils.ListUtils;

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
    public CoreExpression analyze(StaticEnvironment env) {
        return new CoreIfExpression(test.analyze(env), consequent.analyze(env), alternative.analyze(env));
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
