package komu.blunt.asm.opcodes

import komu.blunt.asm.*

class OpPushRegister(private val register: Register) : OpCode() {

    override fun execute(vm: VM?) {
        vm?.push(vm?.get(register))
    }

    override fun modifies(register: Register?) = false
    override fun toString() = "(push $register)"
}
