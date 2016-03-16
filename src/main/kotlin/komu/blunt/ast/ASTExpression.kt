package komu.blunt.ast

abstract class ASTExpression {
    abstract override fun toString(): String
    open fun simplify(): ASTExpression = this
}

