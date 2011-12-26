package komu.blunt.types;

import com.google.common.base.Objects;

import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static komu.blunt.types.Unifier.mguPredicate;

public final class Predicate implements Types<Predicate> {

    final String className;
    final Type type;

    private Predicate(String className, Type type) {
        this.className = checkNotNull(className);
        this.type = checkNotNull(type);
    }

    public boolean inHnf() {
        return type.hnf();
    }

    @Override
    public void addTypeVariables(Set<TypeVariable> variables) {
        type.addTypeVariables(variables);
    }

    @Override
    public Predicate apply(Substitution substitution) {
        return isIn(className, type.apply(substitution));
    }

    public boolean overlaps(Predicate predicate) {
        try {
            mguPredicate(this, predicate);
            return true;
        } catch (UnificationException e) {
            return false;
        }
    }

    public boolean overlapsAny(Iterable<Predicate> predicates) {
        for (Predicate predicate : predicates)
            if (overlaps(predicate))
                return true;
        return false;
    }

    public static Predicate isIn(String className, Type type) {
        return new Predicate(className, type);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;

        if (obj instanceof Predicate) {
            Predicate rhs = (Predicate) obj;

            return className.equals(rhs.className)
                && type.equals(rhs.type);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(className, type);
    }

    @Override
    public String toString() {
        return className + "/" + type;
    }

    public Predicate instantiate(List<TypeVariable> ts) {
        return isIn(className, type.instantiate(ts));
    }
}
