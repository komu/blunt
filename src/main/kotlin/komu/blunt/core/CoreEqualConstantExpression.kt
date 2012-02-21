package komu.blunt.core

import komu.blunt.asm.*

class CoreEqualConstantExpression(private val value: Any?, private val expression: CoreExpression) : CoreExpression() {

    override fun assemble(asm: Assembler, target: Register, linkage: Linkage): Instructions {
        val instructions = Instructions()
        instructions.append(expression.assemble(asm, target, Linkage.NEXT))
        instructions.equalConstant(target, target, value)
        instructions.finishWithLinkage(linkage)
        return instructions
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
