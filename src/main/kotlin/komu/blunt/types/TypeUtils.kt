package komu.blunt.types

import komu.blunt.types.checker.Substitution

object TypeUtils {

    fun addTypeVariables<T : Types<T>>(variables: MutableSet<TypeVariable>, ts: Collection<T>) {
        for (val t in ts)
            t.addTypeVariables(variables)
    }

    fun <T : Types<T>> applySubstitution(substitution: Substitution, ts: Collection<T>): List<T> =
        ts.map { it.apply(substitution) }
}

