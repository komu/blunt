package komu.blunt.types.checker

import komu.blunt.types.Predicate

class PatternTypeCheckResult<out T> constructor(val value: T, val ass: Assumptions = Assumptions.empty, val predicates: List<Predicate> = emptyList()) {

    // TODO this(ass: Assumptions, value: T): this(Collections.emptyList<Predicate>().sure(), ass, value) { }

    override fun toString() = "$predicates/$ass/$value"
}
