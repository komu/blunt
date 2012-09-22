package komu.blunt.ast

import komu.blunt.utils.appendWithSeparator

class ASTLet(val bindings: List<ImplicitBinding>, val body: ASTExpression) : ASTExpression() {

    override fun simplify() =
        ASTLet(bindings.map { it.simplify() }, body.simplify())

    override fun toString() =
        StringBuilder("(let (").appendWithSeparator(bindings, " ").append(") ").append(body).append(')').toString()
}
