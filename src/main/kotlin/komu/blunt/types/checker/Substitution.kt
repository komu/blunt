package komu.blunt.types.checker

import java.util.Collections.emptyMap
import java.util.Collections.singletonMap
import komu.blunt.eval.TypeCheckException
import komu.blunt.types.Type
import komu.blunt.types.TypeGen
import komu.blunt.types.TypeVariable

class Substitution(private val mapping: Map<TypeVariable,Type>) {

    // @@
    fun compose(s2: Substitution): Substitution {
        val map = hashMap<TypeVariable,Type>()

        for ((key, value) in s2.mapping)
            map[key] = value.apply(this)

        map.putAll(mapping)

        return Substitution(map)
    }

    fun merge(s2: Substitution): Substitution {
        if (agree(s2)) {
            val map = hashMap<TypeVariable,Type>()
            map.putAll(mapping)
            map.putAll(s2.mapping)

            return Substitution(map)
        } else
            throw TypeCheckException("merge failed")
    }

    private fun agree(s2: Substitution): Boolean {
        for (v in mapping.keySet())
            if (s2.mapping.containsKey(v))
                if (v.apply(this) != v.apply(s2))
                    return false

        return true
    }

    fun apply(types: List<Type>): List<Type> {
        val result = listBuilder<Type>()

        for (val t in types)
            result.add(t.apply(this))

        return result.build()
    }

    fun apply(subst: Substitution): Substitution {
        val map = hashMap<TypeVariable,Type>()

        for ((key, value) in mapping)
            map[key] = value.apply(subst)

        return Substitution(map)
    }

    fun lookup(variable: TypeVariable): Type? =
        mapping.get(variable)
}

object Substitutions {

    fun empty() = Substitution(emptyMap())

    fun singleton(v: TypeVariable, t: Type): Substitution {
        if (v.kind != t.kind)
            throw IllegalArgumentException()

        return Substitution(singletonMap(v, t))
    }

    fun fromTypeVariables(variables: List<TypeVariable>): Substitution {
        val map = hashMap<TypeVariable,Type>()

        var index = 0
        for (v in variables)
            map[v] = TypeGen(index++)

        return Substitution(map)
    }
}
