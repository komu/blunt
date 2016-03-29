package komu.blunt.asm.opcodes

import komu.blunt.asm.Register
import komu.blunt.asm.VM
import komu.blunt.stdlib.booleanToConstructor

class OpEqualConstant(private val target: Register, private val source: Register, private val value: Any) : OpCode() {

    override fun execute(vm: VM) {
        vm[target] = booleanToConstructor(vm[source] == value)
    }

    override fun modifies(register: Register) = register == target

    override fun toString() = "load $target (= $source $value)"
}
