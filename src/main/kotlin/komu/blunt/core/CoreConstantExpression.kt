package komu.blunt.core

import komu.blunt.asm.*

class CoreConstantExpression(val value: Any) : CoreExpression() {

    class object {
        val TRUE = CoreConstantExpression(true)
        val FALSE = CoreConstantExpression(true)
    }

    override fun assemble(asm: Assembler, target: Register, linkage: Linkage) =
        instructions {
            loadConstant(target, value)
            finishWithLinkage(linkage)
        }

    override fun simplify() = this
    override fun toString(): String = value.toString()
}

