package komu.blunt.types.checker

import komu.blunt.eval.TypeCheckException
import komu.blunt.objects.Symbol
import komu.blunt.types.Scheme
import komu.blunt.types.Type
import komu.blunt.types.Types
import java.util.*
import java.util.Collections.emptyMap
import java.util.Collections.singletonMap

class Assumptions (private val mappings: Map<Symbol,Scheme>) : Types<Assumptions> {

    operator fun plus(ass: Assumptions): Assumptions {
        val b = builder()
        b += ass
        b += this
        return b.build()
    }

    fun augment(arg: Symbol, scheme: Scheme): Assumptions =
        this + singleton(arg, scheme)

    operator fun get(name: Symbol): Scheme =
        mappings[name] ?: throw TypeCheckException("unbound identifier: '$name'")

    override fun toString() = mappings.toString()

    override fun typeVars(): Sequence<Type.Var> =
        mappings.values.asSequence().flatMap { it.typeVars() }

    override fun apply(substitution: Substitution): Assumptions {
        val builder = builder()

        for ((key, value) in mappings)
            builder[key] = value.apply(substitution)

        return builder.build()
    }

    companion object {

        fun builder() = Builder()
        val empty = Assumptions(emptyMap())
        fun singleton(arg: Symbol, scheme: Scheme) = Assumptions(singletonMap(arg, scheme))

        fun from(pairs: Iterable<Pair<Symbol,Scheme>>): Assumptions {
            val builder = Builder()

            for ((name, scheme) in pairs)
                builder[name] = scheme

            return builder.build()
        }

        class Builder {

            private var mappings = HashMap<Symbol,Scheme>()
            private var built = false

            operator fun set(name: Symbol, scheme: Scheme) {
                ensurePrivateCopy()
                mappings[name] = scheme
            }

            operator fun plusAssign(ass: Assumptions) {
                ensurePrivateCopy()
                mappings.putAll(ass.mappings)
            }

            fun build(): Assumptions {
                built = true
                return Assumptions(mappings)
            }

            fun build(ass: Assumptions) = build() + ass

            private fun ensurePrivateCopy() {
                if (built) {
                    mappings = HashMap(mappings)
                    built = false
                }
            }
        }
    }
}
