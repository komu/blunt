package komu.blunt.types.checker

import komu.blunt.eval.TypeCheckException
import komu.blunt.types.Type
import komu.blunt.types.TypeGen
import komu.blunt.types.TypeVariable
import java.util.*
import java.util.Collections.emptyMap
import java.util.Collections.singletonMap

class Substitution(private val mapping: Map<TypeVariable,Type>) {

    fun compose(s2: Substitution): Substitution {
        val map = HashMap<TypeVariable,Type>()

        for ((key, value) in s2.mapping)
            map[key] = value.apply(this)

        map.putAll(mapping)

        return Substitution(map)
    }

    fun merge(s2: Substitution): Substitution {
        if (agree(s2)) {
            val map = HashMap<TypeVariable,Type>()
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
        val map = HashMap<TypeVariable,Type>()

        for ((key, value) in mapping)
            map[key] = value.apply(subst)

        return Substitution(map)
    }

    fun lookup(variable: TypeVariable): Type? =
        mapping[variable]
}

object Substitutions {

    fun empty() = Substitution(emptyMap())

    fun singleton(v: TypeVariable, t: Type): Substitution {
        require(v.kind == t.kind) { "kinds don't match" }

        return Substitution(singletonMap(v, t))
    }

    fun fromTypeVariables(variables: List<TypeVariable>): Substitution {
        val map = HashMap<TypeVariable,Type>()

        variables.forEachIndexed { i, v ->
            map[v] = TypeGen(i)
        }

        return Substitution(map)
    }
}
