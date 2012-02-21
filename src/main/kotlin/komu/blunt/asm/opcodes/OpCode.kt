package komu.blunt.asm.opcodes

import komu.blunt.asm.Register
import komu.blunt.asm.VM

abstract class OpCode {
    abstract fun execute(vm: VM)
    abstract fun modifies(register: Register): Boolean
    abstract fun toString(): String
}
