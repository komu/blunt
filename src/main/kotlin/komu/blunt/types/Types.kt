package komu.blunt.types

import komu.blunt.types.checker.Substitution
import java.util.*

interface Types<out T : Types<T>> {
    fun addTypeVariables(result: MutableSet<TypeVariable>)
    fun apply(substitution: Substitution): T

    val typeVariables: Set<TypeVariable>
        get() {
            val types = LinkedHashSet<TypeVariable>()
            addTypeVariables(types)
            return types
        }
}
