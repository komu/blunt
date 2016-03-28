package komu.blunt.analyzer

import komu.blunt.objects.Symbol

data class VariableInfo(val name: Symbol, val offset: Int) {

    init {
        require(offset >= 0) { "negative offset $offset" }
    }

    fun toReference(frame: Int) = VariableReference.nested(frame, offset, name)

    override fun toString() = name.toString()
}
