package komu.blunt.core

import komu.blunt.asm.*

class CoreEmptyExpression private () : CoreExpression() {

    class object {
        val INSTANCE = CoreEmptyExpression()
    }

    override fun assemble(asm: Assembler, target: Register, linkage: Linkage) =
        instructions {
            finishWithLinkage(linkage)
        }

    override fun simplify() = this

    override fun toString() = "#empty"
}
