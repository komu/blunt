package komu.blunt.ast

class ASTCase(val exp: ASTExpression, val alternatives: List<ASTAlternative>) : ASTExpression() {

    override fun toString() = "case $exp of $alternatives"

    override fun simplify(): ASTExpression =
        ASTCase(exp.simplify(), alternatives.map { it.simplify() })
}
