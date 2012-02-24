package komu.blunt.core

import komu.blunt.asm.*

class CoreConstantExpression(val value: Any) : CoreExpression() {

    override fun assemble(asm: Assembler, target: Register, linkage: Linkage): Instructions {
        val instructions = Instructions()
        instructions.loadConstant(target, value)
        instructions.finishWithLinkage(linkage)
        return instructions
    }

    override fun simplify() = this
    override fun toString(): String = value.toString()
}

