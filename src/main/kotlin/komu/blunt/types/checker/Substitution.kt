package komu.blunt.types.checker

import com.google.common.collect.ImmutableMap

import komu.blunt.types.Type
import komu.blunt.types.TypeVariable
import komu.blunt.types.TypeGen
import java.util.List

object Substitutions {

    fun empty() = Substitution(ImmutableMap.of<TypeVariable?,Type?>().sure())

    fun singleton(v: TypeVariable, t: Type): Substitution {
        if (v.getKind() != t.getKind())
            throw IllegalArgumentException()

        return Substitution(ImmutableMap.of<TypeVariable?,Type?>(v, t).sure())
    }

    fun fromTypeVariables(variables: List<TypeVariable?>): Substitution {
        val builder = ImmutableMap.builder<TypeVariable?,Type?>().sure()

        var index = 0
        for (val v in variables)
            builder.put(v, TypeGen(index++))

        return Substitution(builder.build().sure())
    }
}
