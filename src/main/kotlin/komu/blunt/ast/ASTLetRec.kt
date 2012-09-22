package komu.blunt.ast

import komu.blunt.utils.appendWithSeparator

class ASTLetRec(val bindings: List<ImplicitBinding>, val body: ASTExpression) : ASTExpression() {

    // TODO: convert letrecs to lets if variable is not referenced in binding
    override fun simplify() =
        ASTLetRec(bindings.map { it.simplify() }, body.simplify())

    override fun toString() =
        StringBuilder("(letrec (").appendWithSeparator(bindings, " ").append(") ").append(body).append(')').toString()
}
