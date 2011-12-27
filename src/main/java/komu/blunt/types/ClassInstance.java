package komu.blunt.types;

import komu.blunt.types.checker.Substitution;

import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ClassInstance implements Types<ClassInstance> {
    final Qualified<Predicate> qual;

    public ClassInstance(Qualified<Predicate> qual) {
        this.qual = checkNotNull(qual);
    }
    
    public List<Predicate> getPredicates() {
        return qual.predicates;
    }

    public Predicate getPredicate() {
        return qual.value;
    }

    @Override
    public void addTypeVariables(Set<TypeVariable> variables) {
        qual.addTypeVariables(variables);
    }

    @Override
    public ClassInstance apply(Substitution substitution) {
        return new ClassInstance(qual.apply(substitution));
    }

    @Override
    public String toString() {
        return qual.toString();
    }
}
