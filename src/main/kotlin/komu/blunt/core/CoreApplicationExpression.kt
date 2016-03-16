package komu.blunt.core

import komu.blunt.asm.*
import komu.blunt.asm.Linkage.Companion.NEXT
import komu.blunt.asm.Linkage.Companion.RETURN
import komu.blunt.asm.Register.*

class CoreApplicationExpression(private val func: CoreExpression,
                                private val arg: CoreExpression) : CoreExpression() {

    override fun assemble(asm: Assembler, target: Register, linkage: Linkage) =
        instructions {
            instructionsOf(func.assemble(asm, PROCEDURE, NEXT))

            preserving(PROCEDURE) {
                instructionsOf(arg.assemble(asm, ARG, NEXT))
            }

            if (linkage == RETURN && target == VAL) {
                applyTail()

            } else {
                val afterCall = asm.newLabel("afterCall");

                // TODO: make pushing env the responsibility of called procedure
                if (linkage != RETURN) pushRegister(ENV)

                // TODO: use label from linkage if possible (depends on callee saving env)
                pushLabel(afterCall)
                apply()
                label(afterCall)
                if (linkage != RETURN) popRegister(ENV)

                if (target != VAL)
                    copy(target, VAL)

                finishWithLinkage(linkage)
            }
        }

    override fun simplify() =
        // TODO: simplify application of lambda
        CoreApplicationExpression(func.simplify(), arg.simplify())

    override fun toString() = "($func $arg)"
}
