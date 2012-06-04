package komu.blunt.types

import komu.blunt.types.checker.Substitution

import java.util.Set

trait Types<T : Types<T>?> {
    fun addTypeVariables(result: Set<TypeVariable>)
    fun apply(s: Substitution): T
}
