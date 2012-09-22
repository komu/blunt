package komu.blunt.analyzer

import java.util.Collections.singletonList
import komu.blunt.objects.Symbol

class StaticEnvironment(private val parent: StaticEnvironment? = null) {

    private val variables = hashMap<Symbol, VariableInfo>()

    fun lookup(name: Symbol) = lookup(name, 0)

    fun lookup(name: Symbol, depth: Int): VariableReference {
        val v = variables[name]
        if (v != null)
            return reference(depth, v.offset, name)
        else if (parent != null)
            return parent.lookup(name, depth+1)
        else
            throw UnboundVariableException(name)
    }

    fun define(name: Symbol): VariableReference {
        if (variables.containsKey(name))
            throw AnalyzationException("Variable '$name' is already defined in this scope.")

        val offset = variables.size
        variables[name] = VariableInfo(name, offset)
        return reference(0, offset, name)
    }

    fun lookupInCurrentScopeOrDefine(name: Symbol): VariableReference {
        val v = variables[name]

        return if (v != null)
            reference(0, v.offset, v.name)
        else
            define(name)
    }

    private fun reference(frame: Int, offset: Int, name: Symbol): VariableReference =
        if (parent == null)
            VariableReference.global(offset, name)
        else
            VariableReference.nested(frame, offset, name)

    fun extend(name: Symbol) =
        extend(singletonList(name))

    fun extend() =
        StaticEnvironment(this)

    fun extend(symbols: List<Symbol>): StaticEnvironment {
        val env = StaticEnvironment(this)

        for (symbol in symbols)
            env.define(symbol)

        return env
    }

    val size: Int
        get() = variables.size
}
