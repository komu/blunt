package komu.blunt.asm.opcodes

import komu.blunt.asm.*

class OpPushLabel(private val label: Label?) : OpCode() {

    override fun execute(vm: VM?) {
        vm?.push(label?.getAddress())
    }

    override fun modifies(register: Register?) = false

    override fun toString() = "(push (label $label))"
}
