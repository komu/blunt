package komu.blunt.types;

import com.google.common.base.Objects;

import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.unmodifiableList;
import static komu.blunt.types.TypeUtils.getTypeVariables;

public final class Qualified<T extends Types<T>> implements Types<Qualified<T>> {
    
    public final List<Predicate> predicates;
    public final T value;

    public Qualified(T value) {
        this(Collections.<Predicate>emptyList(), value);
    }

    public Qualified(List<Predicate> predicates, T value) {
        this.predicates = unmodifiableList(newArrayList(predicates));
        this.value = checkNotNull(value);
    }

    @Override
    public void addTypeVariables(Set<TypeVariable> variables) {
        TypeUtils.addTypeVariables(variables, predicates);

        value.addTypeVariables(variables);
    }

    @Override
    public Qualified<T> apply(Substitution substitution) {
        return new Qualified<T>(TypeUtils.apply(substitution, predicates), value.apply(substitution));
    }

    public static Scheme quantify(Collection<TypeVariable> vs, Qualified<Type> qt) {
        List<Kind> kinds = new ArrayList<Kind>();
        List<TypeVariable> vars = new ArrayList<TypeVariable>();

        for (TypeVariable v : getTypeVariables(qt))
            if (vs.contains(v)) {
                vars.add(v);
                kinds.add(v.getKind());
            }

        return new Scheme(kinds, qt.apply(new Substitution(vars)));
    }

    public static Qualified<Type> instantiate(List<TypeVariable> ts, Qualified<Type> t) {
        List<Predicate> ps = new ArrayList<Predicate>(t.predicates.size());
        for (Predicate p : t.predicates)
            ps.add(p.instantiate(ts));
       
        return new Qualified<Type>(ps, t.value.instantiate(ts));
    }

    @Override
    public String toString() {
        return predicates + " => " + value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        
        if (obj instanceof Qualified<?>) {
            Qualified<?> rhs = (Qualified<?>) obj;
            
            return predicates.equals(rhs.predicates)
                && value.equals(rhs.value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(predicates, value);
    }
}
