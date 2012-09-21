package komu.blunt.ast

import komu.blunt.objects.Symbol

import java.util.ArrayList

class ImplicitBinding(val name: Symbol, val expr: ASTExpression) {

    fun toString() = "[$name $expr]"
    fun simplify() = ImplicitBinding(name, expr.simplify())

    class object {
        fun bindingNames(bs: List<ImplicitBinding>): List<Symbol> =
            bs.map { it.name }
    }
}
