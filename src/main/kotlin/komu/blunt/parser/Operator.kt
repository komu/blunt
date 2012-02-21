package komu.blunt.parser;

import komu.blunt.objects.Symbol;

class Operator(val name: String, val associativity: Associativity, val precedence: Int) {

    fun toSymbol() = Symbol(name)
    fun toString() = name
    fun equals(rhs: Any) = rhs is Operator && name == rhs.name
    fun hashCode() = java.util.Objects.hash(name)

    val isConstructor: Boolean
       get() = name.startsWith(":")
}

