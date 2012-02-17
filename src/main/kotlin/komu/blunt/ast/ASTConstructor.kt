package komu.blunt.ast

class ASTConstructor(val name : String) : ASTExpression() {
    override fun toString() = name
    override fun simplify() = this
}
