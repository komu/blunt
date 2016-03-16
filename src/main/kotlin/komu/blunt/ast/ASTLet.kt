package komu.blunt.ast

class ASTLet(val bindings: List<ImplicitBinding>, val body: ASTExpression) : ASTExpression() {

    override fun simplify() =
        ASTLet(bindings.map { it.simplify() }, body.simplify())

    override fun toString() =
        "(let (${bindings.joinToString(" ")}) $body)"
}
