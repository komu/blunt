package komu.blunt.core

import komu.blunt.asm.*

class CoreEmptyExpression private () : CoreExpression() {

    class object {
        val INSTANCE = CoreEmptyExpression()
    }

    override fun assemble(asm: Assembler, target: Register, linkage: Linkage): Instructions {
        val instructions = Instructions()
        instructions.finishWithLinkage(linkage)
        return instructions
    }

    override fun simplify() = this
}
