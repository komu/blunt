package komu.blunt.types.checker

import kotlin.util.*
import com.google.common.collect.ImmutableMap

import komu.blunt.types.Type
import komu.blunt.types.TypeVariable
import komu.blunt.types.TypeGen
import komu.blunt.eval.TypeCheckException
import java.util.ArrayList

class Substitution(private val mapping: ImmutableMap<TypeVariable,Type>) {

    // @@
    fun compose(s2: Substitution): Substitution {
        val builder = ImmutableMap.builder<TypeVariable,Type>()

        for ((key, value) in s2.mapping)
            builder.put(key, value.apply(this))

        builder.putAll(mapping)

        return Substitution(builder.build())
    }

    fun merge(s2: Substitution): Substitution {
        if (agree(s2)) {
            val builder = ImmutableMap.builder<TypeVariable,Type>()
            builder.putAll(mapping)
            builder.putAll(s2.mapping)

            return Substitution(builder.build())
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
        val builder = ImmutableMap.builder<TypeVariable,Type>()

        for ((key, value) in mapping)
            builder.put(key, value.apply(subst))

        return Substitution(builder.build())
    }

    fun lookup(variable: TypeVariable): Type? =
        mapping.get(variable)
}

object Substitutions {

    fun empty() = Substitution(ImmutableMap.of<TypeVariable,Type>())

    fun singleton(v: TypeVariable, t: Type): Substitution {
        if (v.kind != t.kind)
            throw IllegalArgumentException()

        return Substitution(ImmutableMap.of<TypeVariable,Type>(v, t))
    }

    fun fromTypeVariables(variables: List<TypeVariable>): Substitution {
        val builder = ImmutableMap.builder<TypeVariable,Type>()

        var index = 0
        for (val v in variables)
            builder.put(v, TypeGen(index++))

        return Substitution(builder.build())
    }
}
