package komu.blunt.types.checker;

import java.util.List;

import komu.blunt.ast.ASTDefine;
import komu.blunt.ast.ASTExpression;
import komu.blunt.types.ClassEnv;
import komu.blunt.types.Predicate;
import komu.blunt.types.Qualified;
import komu.blunt.types.Scheme;
import komu.blunt.types.Type;

public final class TypeChecker {

    public Qualified<Type> typeCheck(ASTExpression exp, ClassEnv classEnv, Assumptions as) {
        TypeCheckingVisitor checker = new TypeCheckingVisitor(classEnv);
        TypeCheckResult<Type> result = checker.typeCheck(exp, as);
        List<Predicate> ps = classEnv.reduce(TypeUtils.apply(checker.getSubstitution(), result.predicates));
        Qualified<Type> q = new Qualified<Type>(ps, result.value);
        return q.apply(checker.getSubstitution());
    }

    public Scheme typeCheck(ASTDefine exp, ClassEnv classEnv, Assumptions as) {
        TypeCheckingVisitor checker = new TypeCheckingVisitor(classEnv);
        TypeCheckResult<Type> result = exp.typeCheck(checker, as);
        List<Predicate> ps = classEnv.reduce(TypeUtils.apply(checker.getSubstitution(), result.predicates));
        Qualified<Type> q = new Qualified<Type>(ps, result.value);
        return Qualified.quantifyAll(q.apply(checker.getSubstitution()));
    }
}
