package komu.blunt.types

import java.util.LinkedHashSet
import komu.blunt.types.checker.Substitution

trait Types<T : Types<T>> {
    fun addTypeVariables(result: MutableSet<TypeVariable>)
    fun apply(substitution: Substitution): T

    val typeVariables: Set<TypeVariable>
        get() {
            val types = LinkedHashSet<TypeVariable>()
            addTypeVariables(types)
            return types
        }
}
