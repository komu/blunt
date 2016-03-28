package komu.blunt.core

import komu.blunt.asm.*
import komu.blunt.asm.opcodes.OpJumpIfFalse

class CoreIfExpression(val condition: CoreExpression,
                       val consequent: CoreExpression,
                       val alternative: CoreExpression) : CoreExpression() {

    override fun assemble(asm: Assembler, target: Register, linkage: Linkage) =
        instructions {
            val after = asm.newLabel("if-after")
            val falseBranch = asm.newLabel("if-false")

            val trueLinkage = if (linkage == Linkage.Next) Linkage.Jump(after) else linkage

            // Since the target register is safe to overwrite, we borrow it
            // for evaluating the condition as well.

            instructionsOf(condition.assemble(asm, target, Linkage.Next))

            jumpIfFalse(target, falseBranch)

            instructionsOf(consequent.assemble(asm, target, trueLinkage))
            label(falseBranch)
            instructionsOf(alternative.assemble(asm, target, linkage))
            label(after)

            finishWithLinkage(linkage)
        }

    override fun simplify(): CoreExpression {
        val test = condition.simplify()
        val con = consequent.simplify()
        val alt = alternative.simplify()

        return when {
            test is CoreConstantExpression -> if (OpJumpIfFalse.isFalse(test.value)) alt else con
            con == alt                     -> CoreExpression.sequence(test, con)
            else                           -> CoreIfExpression(test, con, alt)
        }
    }

    override fun toString() = "(if $condition $consequent $alternative)"
}
