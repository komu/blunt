package komu.blunt.types

import komu.blunt.types.checker.Substitution
import java.util.*

interface Types<out T : Types<T>> {
    fun addTypeVariables(result: MutableSet<Type.Var>)
    fun apply(substitution: Substitution): T

    val typeVariables: Set<Type.Var>
        get() {
            val types = LinkedHashSet<Type.Var>()
            addTypeVariables(types)
            return types
        }
}
