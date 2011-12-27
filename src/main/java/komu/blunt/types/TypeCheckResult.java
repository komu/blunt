package komu.blunt.types;

import static java.util.Collections.emptyList;

import java.util.List;

public final class TypeCheckResult<T> {

    public final List<Predicate> predicates;
    public final T value;
    
    public TypeCheckResult(List<Predicate> predicates, T value) {
        this.predicates = predicates;
        this.value = value;
    }
    
    public TypeCheckResult(T value) {
        this.predicates = emptyList();
        this.value = value;
    }

    @Override
    public String toString() {
        return predicates + " => " + value;
    }
}
