package komu.blunt.core

import komu.blunt.asm.Assembler
import komu.blunt.asm.Instructions
import komu.blunt.asm.Linkage
import komu.blunt.asm.Register

import java.util.Objects

class CoreEqualConstantExpression(private val value: Any?, private val expression: CoreExpression?) : CoreExpression() {

    override fun assemble(asm: Assembler?, target: Register?, linkage: Linkage?): Instructions {
        val instructions = Instructions()
        instructions.append(expression?.assemble(asm, target, Linkage.NEXT))
        instructions.equalConstant(target, target, value)
        instructions.finishWithLinkage(linkage)
        return instructions
    }

    override fun toString() = "(= $value $expression)"

    override fun simplify(): CoreExpression {
        val simplified = expression?.simplify()
        if (simplified is CoreConstantExpression) {
            return CoreConstantExpression(Objects.equals(value, simplified.value))
        }
        return CoreEqualConstantExpression(value, simplified)
    }
}
