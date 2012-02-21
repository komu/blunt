package komu.blunt.asm.opcodes

import komu.blunt.analyzer.VariableReference
import komu.blunt.asm.*
import komu.blunt.eval.Environment

class OpStoreVariable(private val variable: VariableReference, private val register: Register) : OpCode() {

    override fun execute(vm: VM?) {
        val env = if (variable.sure().isGlobal()) vm?.globalEnvironment else vm?.get(Register.ENV.sure()) as Environment
        val value = vm?.get(register)
        env?.set(variable, value)
    }

    override fun modifies(register: Register?) = false
    override fun toString() = "(store (variable ${variable.frame} ${variable.offset}) $register) ; ${variable.name}"
}
