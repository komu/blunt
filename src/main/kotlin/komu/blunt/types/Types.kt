package komu.blunt.types

import komu.blunt.types.checker.Substitution

trait Types<T : Types<T>> {
    fun addTypeVariables(result: MutableSet<TypeVariable>)
    fun apply(substitution: Substitution): T
}
