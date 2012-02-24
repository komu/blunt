package komu.blunt.types.checker

import komu.blunt.eval.TypeCheckException
import komu.blunt.objects.Symbol
import komu.blunt.types.Scheme
import komu.blunt.types.TypeVariable
import komu.blunt.types.Types

import java.util.HashMap
import java.util.List
import java.util.Map
import java.util.Set

import java.util.Collections.emptyMap
import java.util.Collections.unmodifiableMap
import std.util.*

class Assumptions private(private val mappings: Map<Symbol,Scheme>) : Types<Assumptions> {

    private this(): this(emptyMap<Symbol,Scheme>().sure()) { }

    fun join(ass: Assumptions) =
        builder().addAll(ass).addAll(this).build()

    fun find(name: Symbol): Scheme {
        val scheme = mappings.get(name)
        if (scheme != null)
            return scheme
        else
            throw TypeCheckException("unbound identifier: '$name'")
    }

    override fun toString() = mappings.toString()

    override fun addTypeVariables(variables: Set<TypeVariable?>?) {
        for (val scheme in mappings.values())
            scheme.addTypeVariables(variables)
    }

    override fun apply(substitution: Substitution?): Assumptions {
        val builder = builder()

        for (val entry in mappings.entrySet())
            builder.add(entry?.getKey().sure(), entry?.getValue()?.apply(substitution).sure())

        return builder.build()
    }

    class object {

        fun builder() = Builder()
        fun empty() = Assumptions()
        fun singleton(arg: Symbol, scheme: Scheme) = builder().add(arg, scheme).build()

        fun from(names: List<Symbol>, schemes: List<Scheme>): Assumptions {
            if (names.size != schemes.size)
                throw IllegalArgumentException("${names.size} != ${schemes.size}")

            val builder = Builder()
            for (val i in names.indices)
                builder.add(names[i], schemes[i])

            return builder.build()
        }

        class Builder {

            private var mappings = HashMap<Symbol,Scheme>()
            private var built = false

            fun add(name: Symbol, scheme: Scheme): Builder {
                ensurePrivateCopy()
                mappings.put(name, scheme)
                return this
            }

            fun addAll(ass: Assumptions): Builder {
                ensurePrivateCopy()
                mappings.putAll(ass.mappings)
                return this
            }

            fun build(): Assumptions {
                built = true
                return Assumptions(unmodifiableMap(mappings).sure())
            }

            fun build(ass: Assumptions) = build().join(ass)

            private fun ensurePrivateCopy() {
                if (built) {
                    mappings = HashMap(mappings)
                    built = false
                }
            }
        }
    }
}
