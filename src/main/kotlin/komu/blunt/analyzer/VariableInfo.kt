package komu.blunt.analyzer

import komu.blunt.objects.Symbol

class VariableInfo(val name: Symbol, val offset: Int) {

    {
        check(offset >= 0, "negative offset $offset")
    }

    fun toString() = name.toString()
}
