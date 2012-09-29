package komu.blunt.ast

class ASTLetRec(val bindings: List<ImplicitBinding>, val body: ASTExpression) : ASTExpression() {

    // TODO: convert letrecs to lets if variable is not referenced in binding
    override fun simplify() =
        ASTLetRec(bindings.map { it.simplify() }, body.simplify())

    override fun toString() =
        "(letrec (${bindings.makeString(" ")}) $body)"
}
