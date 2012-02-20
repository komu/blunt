package komu.blunt.parser;

import komu.blunt.objects.Symbol;

class Operator(val name: String, val associativity: Associativity, val precedence: Int) {

    fun toSymbol() = Symbol.symbol(name).sure()
    fun toString() = name
    fun equals(rhs: Any) = rhs is Operator && name == rhs.name
    fun hashCode() = 0 // TODO: name.hashCode()

    val isConstructor: Boolean
       get() = name.startsWith(":")
}

