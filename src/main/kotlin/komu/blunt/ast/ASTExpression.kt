package komu.blunt.ast

abstract class ASTExpression {
    abstract fun toString(): String
    open fun simplify(): ASTExpression = this
}

