package komu.blunt.core

import komu.blunt.asm.*

class CoreApplicationExpression(private val func: CoreExpression,
                                private val arg: CoreExpression) : CoreExpression() {

    override fun assemble(asm0: Assembler?, target: Register?, linkage: Linkage?): Instructions {
        val asm = asm0.sure()

        val instructions = Instructions()

        instructions.append(func.assemble(asm, Register.PROCEDURE, Linkage.NEXT))
        instructions.append(arg.assemble(asm, Register.ARG, Linkage.NEXT)?.preserving(Register.PROCEDURE))

        if (linkage == Linkage.RETURN && target == Register.VAL) {
            instructions.applyTail();

        } else {
            val afterCall = asm.newLabel("afterCall");

            // TODO: make pushing env the responsibility of called procedure
            if (linkage != Linkage.RETURN) instructions.pushRegister(Register.ENV);

            // TODO: use label from linkage if possible (depends on callee saving env)
            instructions.pushLabel(afterCall);
            instructions.apply();
            instructions.label(afterCall);
            if (linkage != Linkage.RETURN) instructions.popRegister(Register.ENV);

            if (target != Register.VAL)
                instructions.copy(target, Register.VAL);

            instructions.finishWithLinkage(linkage);
        }


        return instructions;
    }

    override fun simplify() =
        // TODO: simplify application of lambda
        CoreApplicationExpression(func.simplify().sure(), arg.simplify().sure())

    override fun toString() = "($func $arg)"
}

