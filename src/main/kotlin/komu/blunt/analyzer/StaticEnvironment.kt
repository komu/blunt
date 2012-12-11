package komu.blunt.analyzer

import komu.blunt.objects.Symbol

class StaticEnvironment(private val parent: StaticEnvironment? = null) {

    private val variables = hashMap<Symbol, VariableInfo>()

    val size: Int
        get() = variables.size

    fun get(name: Symbol): VariableReference =
        get(name, 0)

    fun get(name: Symbol, depth: Int): VariableReference {
        val v = variables[name]
        return when {
            v != null      -> v.toReference(depth)
            parent != null -> parent[name, depth+1]
            else           -> throw UnboundVariableException(name)
        }
    }

    fun define(name: Symbol): VariableReference {
        if (variables.containsKey(name))
            throw AnalyzationException("Variable '$name' is already defined in this scope.")

        val v = VariableInfo(name, variables.size)
        variables[name] = v
        return v.toReference(0)
    }

    fun lookupInCurrentScopeOrDefine(name: Symbol): VariableReference =
        variables[name]?.toReference(0) ?: define(name)

    private fun reference(frame: Int, offset: Int, name: Symbol): VariableReference =
        if (parent == null)
            VariableReference.global(offset, name)
        else
            VariableReference.nested(frame, offset, name)

    fun extend(vararg names: Symbol) =
        extend(names.toList())

    fun extend(symbols: Iterable<Symbol>): StaticEnvironment {
        val env = StaticEnvironment(this)

        for (symbol in symbols)
            env.define(symbol)

        return env
    }
}
