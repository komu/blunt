package komu.blunt.core

import komu.blunt.asm.*
import komu.blunt.asm.opcodes.OpJumpIfFalse

class CoreIfExpression(private val condition: CoreExpression,
                       private val consequent: CoreExpression,
                       private val alternative: CoreExpression) : CoreExpression() {

    override fun assemble(asm: Assembler, target: Register, linkage: Linkage) =
        instructions {
            val after = asm.newLabel("if-after")
            val falseBranch = asm.newLabel("if-false")

            val trueLinkage = if (linkage == Linkage.NEXT) Linkage.jump(after) else linkage

            // Since the target register is safe to overwrite, we borrow it
            // for evaluating the condition as well.

            instructionsOf(condition.assemble(asm, target, Linkage.NEXT))

            jumpIfFalse(target, falseBranch)

            instructionsOf(consequent.assemble(asm, target, trueLinkage))
            label(falseBranch)
            instructionsOf(alternative.assemble(asm, target, linkage))
            label(after)

            finishWithLinkage(linkage)
        }

    override fun simplify(): CoreExpression {
        val simplifiedCondition = condition.simplify()
        val simplifiedConsequent = consequent.simplify()
        val simplifiedAlternative = alternative.simplify()

        return if (simplifiedCondition is CoreConstantExpression)
            if (OpJumpIfFalse.isFalse(simplifiedCondition.value)) simplifiedAlternative else simplifiedConsequent
        else
            CoreIfExpression(simplifiedCondition, simplifiedConsequent, simplifiedAlternative)
    }

    override fun toString() = "(if $condition $consequent $alternative)"
}
