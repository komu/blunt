package komu.blunt.asm.opcodes

import komu.blunt.asm.Label
import komu.blunt.asm.Register
import komu.blunt.asm.VM

class OpJump(private val label: Label) : OpCode() {

    override fun execute(vm: VM) {
        vm.pc = label.address
    }

    override fun modifies(register: Register) = register == Register.PC
    override fun toString() = "jump $label"
}
