package komu.blunt.parser;

import komu.blunt.objects.Symbol

class Operator(val name: String, val associativity: Associativity, val precedence: Int) {

    fun toSymbol() = Symbol(name)
    override fun toString() = name
    override fun equals(rhs: Any?) = rhs is Operator && name == rhs.name
    override fun hashCode() = name.hashCode()

    val isConstructor: Boolean
       get() = name.startsWith(":")
}

