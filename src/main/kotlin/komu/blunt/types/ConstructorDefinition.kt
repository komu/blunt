package komu.blunt.types

import com.google.common.base.Preconditions.checkArgument

class ConstructorDefinition(val index: Int, val name: String, val scheme: Scheme, val arity: Int) {

    {
        checkArgument(index >= 0)
        checkArgument(arity >= 0)
    }

    fun toString() = "$name :: $scheme"
}

