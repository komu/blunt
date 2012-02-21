package komu.blunt.asm.opcodes

import komu.blunt.asm.*

class OpCopyRegister(private val target: Register, private val source: Register) : OpCode() {

    override fun execute(vm: VM) {
        vm.set(target, vm.get(source))
    }

    override fun modifies(register: Register) = register == target

    override fun toString() = "(copy $target $source)"
}
