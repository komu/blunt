package komu.blunt.asm.opcodes

import komu.blunt.asm.*

class OpPopRegister(private val target: Register) : OpCode() {

    override fun execute(vm: VM) {
        vm.set(target, vm.pop())
    }

    override fun modifies(register: Register) = register == target

    override fun toString() = "(pop $target)"
}
