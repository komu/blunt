package komu.blunt.types

import komu.blunt.types.checker.Substitution

interface Types<out T : Types<T>> {
    fun apply(substitution: Substitution): T

    /**
     * Returns a sequence of all type variables inside this type.
     * May contain duplicates.
     */
    fun typeVars(): Sequence<Type.Var>

    /**
     * Returns a set of all type variables inside this type.
     */
    fun typeVarsSet(): Set<Type.Var> = typeVars().toSet()
}
