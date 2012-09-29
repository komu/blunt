package komu.blunt.types.checker

import java.util.Collections.emptyMap
import java.util.Collections.singletonMap
import java.util.HashMap
import komu.blunt.eval.TypeCheckException
import komu.blunt.objects.Symbol
import komu.blunt.types.Scheme
import komu.blunt.types.TypeVariable
import komu.blunt.types.Types

class Assumptions (private val mappings: Map<Symbol,Scheme>) : Types<Assumptions> {

    fun join(ass: Assumptions) =
        builder().addAll(ass).addAll(this).build()

    fun find(name: Symbol): Scheme =
        mappings[name] ?: throw TypeCheckException("unbound identifier: '$name'")

    fun toString() = mappings.toString()

    override fun addTypeVariables(result: MutableSet<TypeVariable>) {
        for (scheme in mappings.values())
            scheme.addTypeVariables(result)
    }

    override fun apply(substitution: Substitution): Assumptions {
        val builder = builder()

        for ((key, value) in mappings)
            builder[key] = value.apply(substitution)

        return builder.build()
    }

    class object {

        fun builder() = Builder()
        fun empty() = Assumptions(emptyMap())
        fun singleton(arg: Symbol, scheme: Scheme) = Assumptions(singletonMap(arg, scheme))

        fun from(names: List<Symbol>, schemes: List<Scheme>): Assumptions {
            if (names.size != schemes.size)
                throw IllegalArgumentException("${names.size} != ${schemes.size}")

            val builder = Builder()
            for (i in names.indices)
                builder[names[i]] = schemes[i]

            return builder.build()
        }

        class Builder {

            private var mappings = HashMap<Symbol,Scheme>()
            private var built = false

            fun set(name: Symbol, scheme: Scheme) {
                ensurePrivateCopy()
                mappings[name] = scheme
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
