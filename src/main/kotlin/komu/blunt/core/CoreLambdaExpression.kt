package komu.blunt.core

import komu.blunt.asm.*

import com.google.common.base.Preconditions.checkArgument

class CoreLambdaExpression(private val envSize: Int, private val body: CoreExpression?) : CoreExpression() {

    {
        checkArgument(envSize >= 0)
    }

    override fun assemble(asm0: Assembler?, target: Register?, linkage: Linkage?): Instructions {
        val asm = asm0.sure()
        val instructions = Instructions()

        // TODO: place the lambda in a new code section
        val lambda = asm.newLabel("lambda")
        val afterLambda = asm.newLabel("after-lambda")

        instructions.loadLambda(target, lambda)
        if (linkage == Linkage.NEXT)
            instructions.jump(afterLambda)
        else
            instructions.finishWithLinkage(linkage)

        instructions.label(lambda)
        instructions.createEnvironment(envSize)
        instructions.append(body?.assemble(asm, Register.VAL, Linkage.RETURN).sure())
        instructions.label(afterLambda)

        return instructions
    }

    override fun simplify() = CoreLambdaExpression(envSize, body?.simplify())
}
