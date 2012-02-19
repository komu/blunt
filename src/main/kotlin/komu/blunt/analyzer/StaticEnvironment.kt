package komu.blunt.analyzer

import komu.blunt.objects.Symbol

import java.util.HashMap
import java.util.List
import java.util.Map
import java.util.Arrays

class StaticEnvironment(private val parent: StaticEnvironment? = null) {

    private val variables = HashMap<Symbol, VariableInfo>()

    fun lookup(name: Symbol) = lookup(name, 0)

    fun lookup(name: Symbol, depth: Int): VariableReference {
        val v = variables.get(name);
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

        val offset = variables.size()
        variables.put(name, VariableInfo(name, offset))
        return reference(0, offset, name);
    }

    fun lookupInCurrentScopeOrDefine(name: Symbol): VariableReference {
        val v = variables.get(name)
        return if (v != null)
            reference(0, v.offset, v.name.sure())
        else
            define(name)
    }

    private fun reference(frame: Int, offset: Int, name: Symbol): VariableReference =
        if (parent == null)
            VariableReference.global(offset, name).sure()
        else
            VariableReference.nested(frame, offset, name).sure()

    fun extend(name: Symbol) =
        extend(Arrays.asList<Symbol?>(name).sure())

    fun extend() =
        StaticEnvironment(this)

    fun extend(symbols: List<Symbol?>): StaticEnvironment {
        val env = StaticEnvironment(this)

        for (val symbol in symbols)
            env.define(symbol.sure())

        return env
    }

    fun size() = variables.size()
}

