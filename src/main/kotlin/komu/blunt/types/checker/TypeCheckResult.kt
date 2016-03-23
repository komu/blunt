package komu.blunt.types.checker

import komu.blunt.types.Predicate
import java.util.Collections.emptyList

data class TypeCheckResult<out T>(val value: T, val predicates: List<Predicate> = emptyList()) {

    operator fun plus(predicates: List<Predicate>): TypeCheckResult<T> =
        TypeCheckResult(value, predicates + this.predicates)

    override fun toString() = "$predicates => $value"
}
