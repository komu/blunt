package komu.blunt.types

import java.util.*
import komu.blunt.types.checker.Substitution

object TypeUtils {

    fun addTypeVariables<T : Types<T>>(variables: Set<TypeVariable>, ts: Collection<T?>) {
        for (val t in ts)
            t?.addTypeVariables(variables)
    }

    fun <T : Types<T>> applySubstitution(substitution: Substitution, ts: Collection<T>): List<T> {
        val result = ArrayList<T>(ts.size())

        for (val t in ts)
            result.add(t.apply(substitution).sure())

        return result
    }
}

