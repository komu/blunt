package komu.blunt.ast

import komu.blunt.objects.Symbol
import komu.blunt.types.Scheme
import komu.blunt.types.Type

class ImplicitBinding(val name: Symbol, val expr: ASTExpression) {
    override fun toString() = "[$name $expr]"
    fun simplify() = ImplicitBinding(name, expr.simplify())
}

fun List<ImplicitBinding>.names(): List<Symbol> = map { it.name }

fun List<Pair<ImplicitBinding, Type>>.instantiate(): List<Pair<Symbol, Scheme>> =
    map { Pair(it.first.name, it.second.toScheme()) }
