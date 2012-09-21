package komu.blunt.eval

import komu.blunt.analyzer.StaticEnvironment
import komu.blunt.objects.Symbol
import komu.blunt.parser.TypeParser
import komu.blunt.types.DataTypeDefinitions
import komu.blunt.types.Scheme
import komu.blunt.types.checker.Assumptions

class RootBindings {
    val staticEnvironment = StaticEnvironment()
    val runtimeEnvironment = RootEnvironment()
    private val assumptions = Assumptions.builder()
    val dataTypes = DataTypeDefinitions()

    fun bind(name: String, scheme: Scheme, value: Any?) {
        bind(Symbol(name), scheme, value)
    }

    fun bind(name: Symbol, scheme: Scheme, value: Any?) {
        val ref = staticEnvironment.define(name)
        defineVariableType(name, scheme)
        runtimeEnvironment.define(ref, value)
    }

    fun bind(name: String, scheme: String, value: Any?) {
        bind(name, TypeParser.parseScheme(scheme), value)
    }

    fun bindFunction(name: String, scheme: String, value: (Any?) -> Any?) {
        bind(name, TypeParser.parseScheme(scheme), value)
    }

    fun defineVariableType(name: Symbol, scheme: Scheme) {
        assumptions.add(name, scheme)
    }

    fun createAssumptions(): Assumptions =
        assumptions.build()
}
