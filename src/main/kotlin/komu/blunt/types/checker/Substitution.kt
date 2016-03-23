package komu.blunt.types.checker

import komu.blunt.eval.TypeCheckException
import komu.blunt.types.Type
import java.util.*
import java.util.Collections.emptyMap
import java.util.Collections.singletonMap

class Substitution(private val mapping: Map<Type.Var,Type>) {

    fun compose(s2: Substitution): Substitution {
        val map = HashMap<Type.Var,Type>()

        for ((key, value) in s2.mapping)
            map[key] = value.apply(this)

        map.putAll(mapping)

        return Substitution(map)
    }

    fun merge(s2: Substitution): Substitution {
        if (agree(s2)) {
            val map = HashMap<Type.Var,Type>()
            map.putAll(mapping)
            map.putAll(s2.mapping)

            return Substitution(map)
        } else
            throw TypeCheckException("merge failed")
    }

    private fun agree(s2: Substitution): Boolean {
        for (v in mapping.keys)
            if (v in s2.mapping)
                if (v.apply(this) != v.apply(s2))
                    return false

        return true
    }

    fun apply(types: List<Type>): List<Type> =
        types.map { it.apply(this) }

    fun apply(subst: Substitution): Substitution {
        val map = HashMap<Type.Var,Type>()

        for ((key, value) in mapping)
            map[key] = value.apply(subst)

        return Substitution(map)
    }

    fun lookup(variable: Type.Var): Type? =
        mapping[variable]
}

object Substitutions {

    val empty = Substitution(emptyMap())

    fun singleton(v: Type.Var, t: Type): Substitution {
        require(v.kind == t.kind) { "kinds don't match" }

        return Substitution(singletonMap(v, t))
    }

    fun fromTypeVariables(variables: Iterable<Type.Var>): Substitution =
        Substitution(variables.mapIndexed { i, v -> Pair(v, Type.Gen(i)) }.toMap())
}
