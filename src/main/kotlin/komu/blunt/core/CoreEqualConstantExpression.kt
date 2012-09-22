package komu.blunt.core

import komu.blunt.asm.*

class CoreEqualConstantExpression(private val value: Any?, private val expression: CoreExpression) : CoreExpression() {

    override fun assemble(asm: Assembler, target: Register, linkage: Linkage) =
        instructions {
            instructionsOf(expression.assemble(asm, target, Linkage.NEXT))
            equalConstant(target, target, value)
            finishWithLinkage(linkage)
        }

    override fun toString() = "(= $value $expression)"

    override fun simplify(): CoreExpression {
        val simplified = expression.simplify()
        return if (simplified is CoreConstantExpression)
            CoreConstantExpression(value == simplified.value)
        else
            CoreEqualConstantExpression(value, simplified)
    }
}
