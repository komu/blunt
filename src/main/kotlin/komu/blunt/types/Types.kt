package komu.blunt.types

import java.util.LinkedHashSet
import komu.blunt.types.checker.Substitution

trait Types<T : Types<T>> {
    fun addTypeVariables(result: MutableSet<TypeVariable>)
    fun apply(substitution: Substitution): T

    fun getTypeVariables(): Set<TypeVariable> {
        val types = LinkedHashSet<TypeVariable>()
        addTypeVariables(types)
        return types
    }

    fun containsVariable(v: TypeVariable): Boolean =
        getTypeVariables().contains(v)
}
