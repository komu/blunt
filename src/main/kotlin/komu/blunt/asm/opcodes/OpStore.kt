package komu.blunt.asm.opcodes

import komu.blunt.analyzer.VariableReference
import komu.blunt.asm.Register
import komu.blunt.asm.VM

sealed class OpStore() : OpCode() {

    override fun modifies(register: Register) = false

    class LocalVariable(val register: Register, val variable: VariableReference) : OpStore() {

        init {
            require(!variable.global)
        }

        override fun execute(vm: VM) {
            vm.env[variable] = vm[register]
        }

        override fun toString() = "(store (variable ${variable.frame} ${variable.offset}) $register) ; ${variable.name}"
    }

    class GlobalVariable(val register: Register, val variable: VariableReference) : OpStore() {

        init {
            require(variable.global)
        }

        override fun execute(vm: VM) {
            vm.globalEnvironment[variable] = vm[register]
        }

        override fun toString() = "(store (global-variable ${variable.offset}) $register) ; ${variable.name}"
    }
}
