package komu.blunt.core

import com.google.common.base.Preconditions.checkArgument
import komu.blunt.asm.*

class CoreLambdaExpression(private val envSize: Int, private val body: CoreExpression) : CoreExpression() {

    {
        checkArgument(envSize >= 0)
    }

    override fun assemble(asm: Assembler, target: Register, linkage: Linkage): Instructions {
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
        instructions.append(body.assemble(asm, Register.VAL, Linkage.RETURN))
        instructions.label(afterLambda)

        return instructions
    }

    override fun simplify() = CoreLambdaExpression(envSize, body.simplify())

    override fun toString() = "(lambda $envSize $body)"
}
