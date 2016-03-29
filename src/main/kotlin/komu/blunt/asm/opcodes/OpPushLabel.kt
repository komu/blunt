package komu.blunt.asm.opcodes

import komu.blunt.asm.Label
import komu.blunt.asm.Register
import komu.blunt.asm.VM

class OpPushLabel(private val label: Label) : OpCode() {

    override fun execute(vm: VM) {
        vm.push(label.address)
    }

    override fun modifies(register: Register) = false

    override fun toString() = "push (label $label)"
}
