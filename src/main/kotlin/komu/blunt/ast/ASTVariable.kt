package komu.blunt.ast

import komu.blunt.objects.Symbol

class ASTVariable(val name: Symbol) : ASTExpression() {
    override fun toString() = name.toString()
    override fun simplify() = this
}
