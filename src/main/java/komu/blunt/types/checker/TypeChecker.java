package komu.blunt.types.checker;

import static komu.blunt.types.checker.Unifier.mgu;

import java.util.ArrayList;
import java.util.List;

import komu.blunt.ast.ASTDefine;
import komu.blunt.ast.ASTExpression;
import komu.blunt.eval.TypeCheckException;
import komu.blunt.types.ClassEnv;
import komu.blunt.types.Kind;
import komu.blunt.types.Predicate;
import komu.blunt.types.Qualified;
import komu.blunt.types.Scheme;
import komu.blunt.types.Type;
import komu.blunt.types.TypeVariable;

public final class TypeChecker {

    private Substitution substitution = Substitution.empty();
    private int typeSequence = 0;
    
    public Qualified<Type> typeCheck(ASTExpression exp, ClassEnv classEnv, Assumptions as) {
        TypeCheckingVisitor checker = new TypeCheckingVisitor(classEnv, this);
        TypeCheckResult<Type> result = checker.typeCheck(exp, as);
        List<Predicate> ps = classEnv.reduce(TypeUtils.apply(substitution, result.predicates));
        Qualified<Type> q = new Qualified<Type>(ps, result.value);
        return q.apply(substitution);
    }

    public Scheme typeCheck(ASTDefine exp, ClassEnv classEnv, Assumptions as) {
        TypeCheckingVisitor checker = new TypeCheckingVisitor(classEnv, this);
        TypeCheckResult<Type> result = exp.typeCheck(checker, as);
        List<Predicate> ps = classEnv.reduce(TypeUtils.apply(substitution, result.predicates));
        Qualified<Type> q = new Qualified<Type>(ps, result.value);
        return Qualified.quantifyAll(q.apply(substitution));
    }

    public void unify(Type t1, Type t2) {
        try {
            Substitution u = mgu(t1.apply(substitution), t2.apply(substitution));
            substitution = u.compose(substitution);
        } catch (UnificationException e) {
            throw new TypeCheckException(e);
        }
    }

    public Qualified<Type> freshInstance(Scheme scheme) {
        List<TypeVariable> ts = new ArrayList<TypeVariable>(scheme.kinds.size());
        for (Kind kind : scheme.kinds)
            ts.add(newTVar(kind));

        return Qualified.instantiate(ts, scheme.type);
    }

    public TypeVariable newTVar(Kind kind) {
        return new TypeVariable(typeName(typeSequence++), kind);
    }

    public List<Type> newTVars(int size, Kind kind) {
        List<Type> types = new ArrayList<Type>(size);
        for (int i = 0; i < size; i++)
            types.add(newTVar(kind));
        return types;
    }

    private static String typeName(int index) {
        if (index < 5) {
            return String.valueOf((char) ('a' + index));
        } else {
            return "t" + (index-5);
        }
    }

    public Substitution getSubstitution() {
        return substitution;
    }
}
