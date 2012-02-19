package komu.blunt.eval

import komu.blunt.analyzer.StaticEnvironment
import komu.blunt.analyzer.VariableReference
import komu.blunt.objects.Symbol
import komu.blunt.types.DataTypeDefinitions
import komu.blunt.types.Scheme
import komu.blunt.types.checker.Assumptions

import komu.blunt.objects.Symbol.symbol

class RootBindings {
    val staticEnvironment = StaticEnvironment()
    val runtimeEnvironment = RootEnvironment()
    private val assumptions = Assumptions.builder().sure()
    val dataTypes = DataTypeDefinitions()

    fun bind(name: String?, scheme: Scheme?, value: Any?) {
        bind(symbol(name).sure(), scheme, value)
    }

    fun bind(name: Symbol?, scheme: Scheme?, value: Any?) {
        val ref = staticEnvironment.define(name.sure())
        defineVariableType(name, scheme)
        runtimeEnvironment.define(ref, value)
    }

    fun defineVariableType(name: Symbol?, scheme: Scheme?) {
        assumptions.add(name, scheme)
    }

    fun createAssumptions(): Assumptions =
        assumptions.build().sure()
}
