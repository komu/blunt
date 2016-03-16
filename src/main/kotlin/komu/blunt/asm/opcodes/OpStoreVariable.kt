package komu.blunt.asm.opcodes

import komu.blunt.analyzer.VariableReference
import komu.blunt.asm.Register
import komu.blunt.asm.VM

class OpStoreVariable(private val variable: VariableReference, private val register: Register) : OpCode() {

    override fun execute(vm: VM) {
        val env = if (variable.global) vm.globalEnvironment else vm.env
        env[variable] = vm[register]
    }

    override fun modifies(register: Register) = false
    override fun toString() = "(store (variable ${variable.frame} ${variable.offset}) $register) ; ${variable.name}"
}
