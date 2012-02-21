package komu.blunt.asm.opcodes

import komu.blunt.asm.Register
import komu.blunt.asm.VM

class OpCreateEnvironment(private val envSize: Int) : OpCode() {

    {
        if (envSize < 0) throw IllegalArgumentException("negative envSize: $envSize")
    }

    override fun execute(vm0: VM?) {
        val vm = vm0.sure()
        vm.env = vm.env.extend(envSize, vm.arg).sure()
    }

    override fun modifies(register: Register?) = register == Register.ENV

    override fun toString() = "(load ${Register.ENV} (create-env ${Register.ENV} ${Register.ENV} $envSize))"
}

