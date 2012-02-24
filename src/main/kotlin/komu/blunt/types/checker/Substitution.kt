package komu.blunt.types.checker

import std.util.*
import com.google.common.collect.ImmutableMap

import komu.blunt.types.Type
import komu.blunt.types.TypeVariable
import komu.blunt.types.TypeGen
import java.util.List
import komu.blunt.eval.TypeCheckException
import java.util.ArrayList

object Substitutions {

    fun empty(): Substitution = SubstitutionImpl(ImmutableMap.of<TypeVariable,Type>().sure())

    fun singleton(v: TypeVariable, t: Type): Substitution {
        if (v.getKind() != t.getKind())
            throw IllegalArgumentException()

        return SubstitutionImpl(ImmutableMap.of<TypeVariable,Type>(v, t).sure())
    }

    fun fromTypeVariables(variables: List<TypeVariable?>): Substitution {
        val builder = ImmutableMap.builder<TypeVariable,Type>().sure()

        var index = 0
        for (val v in variables)
            builder.put(v.sure(), TypeGen(index++))

        return SubstitutionImpl(builder.build().sure())
    }
}

class SubstitutionImpl(private val mapping: ImmutableMap<TypeVariable,Type>) : Substitution() {

    // @@
    override fun compose(s2: Substitution?): Substitution {
        val builder = ImmutableMap.builder<TypeVariable,Type>().sure()

        for (val entry in (s2 as SubstitutionImpl).mapping.entrySet())
            builder.put(entry?.getKey().sure(), entry?.getValue()?.apply(this).sure())

        builder.putAll(mapping)

        return SubstitutionImpl(builder.build().sure())
    }

    override fun merge(s2: Substitution?): Substitution {
        if (s2 !is SubstitutionImpl) throw Exception()

        if (agree(s2)) {
            val builder = ImmutableMap.builder<TypeVariable,Type>().sure()
            builder.putAll(mapping)
            builder.putAll(s2.mapping)

            return SubstitutionImpl(builder.build().sure())
        } else
            throw TypeCheckException("merge failed")
    }

    private fun agree(s2: SubstitutionImpl): Boolean {
        for (val v in mapping.keySet())
            if (s2.mapping.containsKey(v))
                if (v.apply(this) != v.apply(s2))
                    return false

        return true
    }

    override fun apply(types: List<Type?>?): List<Type?> {
        val result = ArrayList<Type?>(types.sure().size)

        for (val t in types)
            result.add(t.sure().apply(this))

        return result
    }

    override fun apply(subst: Substitution?): Substitution {
        val builder = ImmutableMap.builder<TypeVariable,Type>().sure()

        for (val entry in mapping.entrySet())
            builder.put(entry?.getKey().sure(), entry?.getValue()?.apply(subst).sure());

        return SubstitutionImpl(builder.build().sure())
    }

    override fun lookup(variable: TypeVariable?): Type? =
        mapping.get(variable.sure())
}
