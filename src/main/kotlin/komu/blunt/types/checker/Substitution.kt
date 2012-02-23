package komu.blunt.types.checker

import com.google.common.collect.ImmutableMap

import komu.blunt.types.Type
import komu.blunt.types.TypeVariable
import komu.blunt.types.TypeGen
import java.util.List
import komu.blunt.eval.TypeCheckException
import java.util.ArrayList

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

class Substitution(private val mapping: ImmutableMap<TypeVariable?, Type?>) {

    // @
    fun compose(s2: Substitution): Substitution {
        val builder = ImmutableMap.builder<TypeVariable?,Type?>().sure()

        for (val entry in s2.mapping.entrySet())
            builder.put(entry.sure().getKey(), entry?.getValue().sure().apply(this))

        builder.putAll(mapping)

        return Substitution(builder.build().sure())
    }

    fun merge(s2: Substitution): Substitution {
        if (agree(s2)) {
            val builder = ImmutableMap.builder<TypeVariable?,Type?>().sure()
            builder.putAll(mapping)
            builder.putAll(s2.mapping)

            return Substitution(builder.build().sure())
        } else
            throw TypeCheckException("merge failed")
    }

    private fun agree(s2: Substitution): Boolean {
        for (val v in mapping.keySet())
            if (s2.mapping.containsKey(v))
                if (v?.apply(this) != v?.apply(s2))
                    return false;

        return true;
    }

    fun apply(subst: Substitution): Substitution {
        val builder = ImmutableMap.builder<TypeVariable?,Type?>().sure()

        for (val entry in mapping.entrySet())
            builder.put(entry?.getKey(), entry?.getValue()?.apply(subst));

        return Substitution(builder.build().sure());
    }

    fun lookup(v: TypeVariable): Type =
        mapping.get(v).sure()

    fun apply(types: List<Type>): List<Type> {
        val result = ArrayList<Type>(types.size())

        for (val t in types)
            result.add(t.apply(this))

        return result
    }
}
