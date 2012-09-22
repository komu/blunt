package komu.blunt.types.checker

import java.util.Collections.emptyMap
import java.util.HashMap
import komu.blunt.eval.TypeCheckException
import komu.blunt.objects.Symbol
import komu.blunt.types.Scheme
import komu.blunt.types.TypeVariable
import komu.blunt.types.Types

class Assumptions (private val mappings: Map<Symbol,Scheme>) : Types<Assumptions> {

    fun join(ass: Assumptions) =
            builder().addAll(ass).addAll(this).build()

    fun find(name: Symbol): Scheme {
        val scheme = mappings[name]
        if (scheme != null)
            return scheme
        else
            throw TypeCheckException("unbound identifier: '$name'")
    }

    fun toString() = mappings.toString()

    override fun addTypeVariables(result: MutableSet<TypeVariable>) {
        for (scheme in mappings.values())
            scheme.addTypeVariables(result)
    }

    override fun apply(substitution: Substitution): Assumptions {
        val builder = builder()

        for ((key, value) in mappings)
            builder.add(key, value.apply(substitution))

        return builder.build()
    }

    class object {

        fun builder() = Builder()
        fun empty() = Assumptions(emptyMap())
        fun singleton(arg: Symbol, scheme: Scheme) = builder().add(arg, scheme).build()

        fun from(names: List<Symbol>, schemes: List<Scheme>): Assumptions {
            if (names.size != schemes.size)
                throw IllegalArgumentException("${names.size} != ${schemes.size}")

            val builder = Builder()
            for (i in names.indices)
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
                return Assumptions(mappings)
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
