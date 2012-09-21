package komu.blunt.ast

import komu.blunt.objects.Symbol

class ImplicitBinding(val name: Symbol, val expr: ASTExpression) {
    fun toString() = "[$name $expr]"
    fun simplify() = ImplicitBinding(name, expr.simplify())
}

fun List<ImplicitBinding>.names(): List<Symbol> = map { it.name }
