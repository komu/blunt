package komu.blunt.asm.opcodes

import komu.blunt.asm.*

class OpJump(private val label: Label) : OpCode() {

    override fun execute(vm: VM?) {
        vm.sure().pc = label.getAddress()
    }

    override fun modifies(register: Register?) = register == Register.PC
    override fun toString() = "(jump $label)"
}
