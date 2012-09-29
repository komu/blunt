package komu.blunt.analyzer

import komu.blunt.objects.Symbol

class IdentifierMapping(val parent: IdentifierMapping? = null) {

    private val mappings = hashMap<Symbol,Symbol>()
    private val sequence: Sequence = if (parent != null) parent.seq() else Sequence()

    private fun seq() = sequence

    fun get(v: Symbol): Symbol {
        var mapping = this

        while (true) {
            val sym = mapping.mappings[v]
            if (sym != null)
                return sym

            val parent = mapping.parent
            if (parent == null)
                break
            else
                mapping = parent
        }

        return v
    }

    fun set(oldName: Symbol, newName: Symbol) {
        val old = mappings.put(oldName, newName)
        if (old != null)
            throw IllegalArgumentException("duplicate mapping for '$oldName'");
    }

    fun extend() = IdentifierMapping(this)

    /**
     * Creates a new unique symbol based on name, installs it on the mapping and returns it.
     */
    fun freshMappingFor(name: Symbol): Symbol {
        val v = Symbol("\$${name}_${sequence.next()}")
        this[name] = v
        return v
    }
}

class Sequence {
    private var num = 1

    fun next() = num++
}
