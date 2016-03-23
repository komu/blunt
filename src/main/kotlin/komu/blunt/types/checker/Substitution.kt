package komu.blunt.types.checker

import komu.blunt.eval.TypeCheckException
import komu.blunt.types.Type
import java.util.*
import java.util.Collections.emptyMap
import java.util.Collections.singletonMap

class Substitution(private val mapping: Map<Type.Var,Type>) {

    fun compose(s2: Substitution): Substitution {
        val map = s2.mapping.mapValuesTo(HashMap()) { it.value.apply(this) }

        map.putAll(mapping)

        return Substitution(map)
    }

    fun merge(s2: Substitution): Substitution =
        if (agree(s2))
            Substitution(mapping + s2.mapping)
        else
            throw TypeCheckException("merge failed")

    private fun agree(s2: Substitution): Boolean =
        mapping.keys.all { it in s2.mapping && it.apply(this) != it.apply(s2) }

    fun apply(types: List<Type>): List<Type> =
        types.map { it.apply(this) }

    fun apply(subst: Substitution): Substitution =
        Substitution(mapping.mapValues { it.value.apply(subst) })

    operator fun get(variable: Type.Var): Type? =
        mapping[variable]

    companion object {
        val empty = Substitution(emptyMap())

        fun singleton(v: Type.Var, t: Type): Substitution {
            require(v.kind == t.kind) { "kinds don't match" }

            return Substitution(singletonMap(v, t))
        }

        fun fromTypeVariables(variables: Iterable<Type.Var>): Substitution =
            Substitution(variables.mapIndexed { i, v -> Pair(v, Type.Gen(i)) }.toMap())
    }
}
