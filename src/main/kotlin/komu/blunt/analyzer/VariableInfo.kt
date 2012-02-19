package komu.blunt.analyzer

import komu.blunt.objects.Symbol

class VariableInfo(val name: Symbol, val offset: Int) {

    {
        if (offset < 0) throw IllegalArgumentException("negative offset: $offset")
    }

    fun toString() = name.toString()
}
