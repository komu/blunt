package komu.blunt.asm.opcodes

import komu.blunt.asm.Register
import komu.blunt.asm.VM

class OpCreateEnvironment(private val envSize: Int) : OpCode() {

    {
        check(envSize >= 0)
    }

    override fun execute(vm: VM) {
        vm.env = vm.env.extend(envSize, vm.arg)
    }

    override fun modifies(register: Register) = register == Register.ENV
    override fun toString() = "(load ${Register.ENV} (create-env ${Register.ENV} ${Register.ENV} $envSize))"
}

