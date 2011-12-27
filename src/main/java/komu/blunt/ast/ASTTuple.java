package komu.blunt.ast;

import static komu.blunt.types.Type.tupleType;

import java.util.ArrayList;
import java.util.List;

import komu.blunt.core.CoreExpression;
import komu.blunt.core.CoreTupleExpression;
import komu.blunt.eval.StaticEnvironment;
import komu.blunt.types.Assumptions;
import komu.blunt.types.ClassEnv;
import komu.blunt.types.Predicate;
import komu.blunt.types.Type;
import komu.blunt.types.TypeCheckResult;
import komu.blunt.types.TypeChecker;

public final class ASTTuple extends ASTExpression {
    
    private final List<ASTExpression> exps;

    public ASTTuple(List<ASTExpression> exps) {
        if (exps.size() < 2) throw new IllegalArgumentException("invalid sub expressions for tuple: " + exps);

        this.exps = new ArrayList<ASTExpression>(exps);
    }

    @Override
    public CoreExpression analyze(StaticEnvironment env) {
        return new CoreTupleExpression(analyzeAll(exps, env));
    }

    @Override
    public TypeCheckResult<Type> typeCheck(ClassEnv ce, TypeChecker tc, Assumptions as) {
        // TODO: generalize tuples to type constructors
        List<Predicate> predicates = new ArrayList<Predicate>();
        List<Type> types = new ArrayList<Type>();
        
        for (ASTExpression exp : exps) {
            TypeCheckResult<Type> result = exp.typeCheck(ce, tc, as);
            predicates.addAll(result.predicates);
            types.add(result.value);
        }
        
        return new TypeCheckResult<Type>(predicates, tupleType(types));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("(tuple");
        
        for (ASTExpression exp : exps)
            sb.append(' ').append(exp);
        
        sb.append(")");
        return sb.toString();
    }
}
