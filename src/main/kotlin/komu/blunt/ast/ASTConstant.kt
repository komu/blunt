package komu.blunt.ast

import komu.blunt.types.*

class ASTConstant(val value: Any) : ASTExpression() {
    fun valueType() = typeFromObject(value)
    override fun toString() = value.toString()
    override fun simplify() = this
}
