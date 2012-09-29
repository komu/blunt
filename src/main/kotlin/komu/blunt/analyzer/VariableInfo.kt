package komu.blunt.analyzer

import komu.blunt.objects.Symbol

class VariableInfo(val name: Symbol, val offset: Int) {

    {
        require(offset >= 0, "negative offset $offset")
    }

    fun toReference(frame: Int) = VariableReference.nested(frame, offset, name)

    fun toString() = name.toString()
}
