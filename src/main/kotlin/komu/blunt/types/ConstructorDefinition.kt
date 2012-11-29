package komu.blunt.types

class ConstructorDefinition(val index: Int, val name: String, val scheme: Scheme, val arity: Int) {

    {
        require(index >= 0)
        require(arity >= 0)
    }

    fun toString() = "$name :: $scheme"
}

