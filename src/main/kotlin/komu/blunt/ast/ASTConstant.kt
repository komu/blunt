package komu.blunt.ast

import komu.blunt.types.Type

class ASTConstant(val value: Any) : ASTExpression() {
    fun valueType() = Type.fromObject(value).sure()
    override fun toString() = value.toString()
    override fun simplify() = this
}
