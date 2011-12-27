package komu.blunt.types;

import komu.blunt.objects.Symbol;

import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static komu.blunt.types.TypeUtils.getTypeVariables;

public final class TypeCheckingContext {
    
    private final ClassEnv classEnv;
    private final TypeChecker typeChecker;
    public final Assumptions assumptions;

    public TypeCheckingContext(ClassEnv classEnv, TypeChecker typeChecker, Assumptions assumptions) {
        this.classEnv = checkNotNull(classEnv);
        this.typeChecker = checkNotNull(typeChecker);
        this.assumptions = checkNotNull(assumptions);
    }
    
    public TypeCheckingContext extend(Assumptions as) {
        return new TypeCheckingContext(classEnv, typeChecker, as.join(this.assumptions));
    }

    public Scheme find(Symbol var) {
        return assumptions.find(var);
    }

    public Set<TypeVariable> typeVariables() {
        return getTypeVariables(assumptions.apply(typeChecker.getSubstitution()));
    }

    public <T extends Types<T>> List<T> apply(List<T> pss) {
        return TypeUtils.apply(typeChecker.getSubstitution(), pss);
    }

    public List<Predicate> reduce(List<Predicate> ps) {
        return classEnv.reduce(ps);
    }

    public TypeVariable newTVar(Kind kind) {
        return typeChecker.newTVar(kind);
    }

    public List<Type> newTVars(int size, Kind kind) {
        return typeChecker.newTVars(size, kind);
    }

    public void unify(Type t1, Type t2) {
        typeChecker.unify(t1, t2);
    }

    public Qualified<Type> freshInstance(Scheme scheme) {
        return typeChecker.freshInstance(scheme);
    }
}
