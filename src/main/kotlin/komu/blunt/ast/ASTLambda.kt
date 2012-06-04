package komu.blunt.ast

import komu.blunt.objects.Symbol

class ASTLambda(val argument: Symbol, val body: ASTExpression) : ASTExpression() {
    override fun toString() = "(lambda $argument $body)"
    override fun simplify() = ASTLambda(argument, body.simplify())
}
