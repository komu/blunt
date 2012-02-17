package komu.blunt.types.checker

import komu.blunt.types.Predicate

import java.util.Collections
import java.util.List

class PatternTypeCheckResult<out T>(val predicates: List<Predicate?>, val ass: Assumptions, val value: T) {

    this(ass: Assumptions, value: T): this(Collections.emptyList<Predicate?>().sure(), ass, value) { }

    fun toString() = "$predicates/$ass/$value"
}
