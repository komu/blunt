package komu.blunt.ast

import komu.blunt.objects.Symbol

class ASTSet(val variable: Symbol, val exp: ASTExpression) : ASTExpression() {
    override fun toString() = "(set! $variable $exp)"
    override fun simplify() = ASTSet(variable, exp.simplify().sure())
}
