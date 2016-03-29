package komu.blunt.asm.opcodes

import komu.blunt.asm.Register
import komu.blunt.asm.VM

class OpCopyRegister(private val target: Register, private val source: Register) : OpCode() {

    override fun execute(vm: VM) {
        vm[target] = vm[source]
    }

    override fun modifies(register: Register) = register == target
    override fun toString() = "copy $target $source"
}
