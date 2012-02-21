package komu.blunt.asm.opcodes

import komu.blunt.asm.Label
import komu.blunt.asm.Register
import komu.blunt.asm.VM
import komu.blunt.objects.TypeConstructorValue
import komu.blunt.types.ConstructorNames

class OpJumpIfFalse(private val register: Register, private val label: Label) : OpCode() {

    class object {
        // TODO: move this
        fun isFalse(value: Any?): Boolean =
            false == value || (value is TypeConstructorValue && value.name == ConstructorNames.FALSE)
    }

    override fun execute(vm0: VM?) {
        val vm = vm0.sure()
        val value = vm.get(register)
        if (isFalse(value))
            vm.pc = label.getAddress();
    }

    override fun modifies(register: Register?) = register == Register.PC

    override fun toString() = "(jump-if-false $register $label)"
}

