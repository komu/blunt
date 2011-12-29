package komu.blunt.types.checker;

import komu.blunt.types.Predicate;

import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

final class PatternTypeCheckResult<T> {

    final List<Predicate> predicates;
    final Assumptions as;
    final T value;

    PatternTypeCheckResult(List<Predicate> predicates, Assumptions as, T type) {
        this.predicates = checkNotNull(predicates);
        this.as = checkNotNull(as);
        this.value = checkNotNull(type);
    }

    PatternTypeCheckResult(Assumptions as, T type) {
        this.predicates = Collections.emptyList();
        this.as = checkNotNull(as);
        this.value = checkNotNull(type);
    }

    @Override
    public String toString() {
        return predicates + "/" + as + "/" + value;
    }
}
