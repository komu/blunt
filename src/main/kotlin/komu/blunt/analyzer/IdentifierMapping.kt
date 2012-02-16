package komu.blunt.analyzer

import komu.blunt.objects.Symbol

import java.util.HashMap

private class IdentifierMapping(val parent: IdentifierMapping?) {

    private val mappings = HashMap<Symbol,Symbol>()

    this(): this(null) { }

    public fun get(v: Symbol?): Symbol {
        var mapping = this
        while (true) {
            val sym = mapping.mappings.get(v)
            if (sym != null)
                return sym

            val parent = mapping.parent
            if (parent == null)
                break
            else
                mapping = parent
        }

        return v.sure()
    }

    public fun put(oldName: Symbol?, newName: Symbol?) {
        val old = mappings.put(oldName.sure(), newName.sure())
        if (old != null)
            throw IllegalArgumentException("duplicate mapping for '$oldName'");
    }

    public fun extend(): IdentifierMapping =
        IdentifierMapping(this)
}
