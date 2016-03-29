package komu.blunt.asm.opcodes

import komu.blunt.asm.Register
import komu.blunt.asm.VM

class OpPushRegister(private val register: Register) : OpCode() {

    override fun execute(vm: VM) {
        vm.push(vm[register])
    }

    override fun modifies(register: Register) = false
    override fun toString() = "push $register"
}
