package komu.blunt.asm.opcodes

import komu.blunt.asm.Register
import komu.blunt.asm.VM

class OpPopRegister(private val target: Register) : OpCode() {

    override fun execute(vm: VM) {
        vm[target] = vm.pop()
    }

    override fun modifies(register: Register) = register == target

    override fun toString() = "pop $target"
}
