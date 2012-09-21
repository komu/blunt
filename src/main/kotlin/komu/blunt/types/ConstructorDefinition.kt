package komu.blunt.types

class ConstructorDefinition(val index: Int, val name: String, val scheme: Scheme, val arity: Int) {

    {
        check(index >= 0)
        check(arity >= 0)
    }

    fun toString() = "$name :: $scheme"
}

