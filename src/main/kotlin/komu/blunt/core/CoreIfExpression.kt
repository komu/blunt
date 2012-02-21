package komu.blunt.core

import komu.blunt.asm.*

import komu.blunt.asm.opcodes.OpJumpIfFalse

class CoreIfExpression(private val condition: CoreExpression?,
                       private val consequent: CoreExpression?,
                       private val alternative: CoreExpression?) : CoreExpression() {

    override fun assemble(asm0: Assembler?, target: Register?, linkage: Linkage?): Instructions {
        val asm = asm0.sure()
        val instructions = Instructions()

        val after = asm.newLabel("if-after")
        val falseBranch = asm.newLabel("if-false")

        val trueLinkage = if (linkage == Linkage.NEXT) Linkage.jump(after) else linkage

        // Since the target register is safe to overwrite, we borrow it
        // for evaluating the condition as well.
        instructions.append(condition?.assemble(asm, target, Linkage.NEXT))
        instructions.jumpIfFalse(target, falseBranch)

        instructions.append(consequent?.assemble(asm, target, trueLinkage))
        instructions.label(falseBranch)
        instructions.append(alternative?.assemble(asm, target, linkage))
        instructions.label(after)

        instructions.finishWithLinkage(linkage)

        return instructions
    }

    override fun simplify(): CoreExpression {
        val simplifiedCondition = condition?.simplify()
        val simplifiedConsequent = consequent?.simplify().sure()
        val simplifiedAlternative = alternative?.simplify().sure()

        return if (simplifiedCondition is CoreConstantExpression)
            if (OpJumpIfFalse.isFalse(simplifiedCondition.value)) simplifiedAlternative else simplifiedConsequent
        else
            CoreIfExpression(simplifiedCondition, simplifiedConsequent, simplifiedAlternative)
    }

    override fun toString() = "(if $condition $consequent $alternative)"
}
