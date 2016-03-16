package komu.blunt.asm.opcodes

import komu.blunt.asm.Register
import komu.blunt.asm.VM
import komu.blunt.objects.CompoundProcedure
import komu.blunt.objects.EvaluationException
import komu.blunt.objects.PrimitiveProcedure

class OpApply private constructor(private val tail: Boolean) : OpCode() {

    companion object {
        val NORMAL = OpApply(false)
        val TAIL   = OpApply(true)
    }

    override fun execute(vm: VM) {
        val procedure = vm.procedure

        when (procedure) {
            is PrimitiveProcedure -> executePrimitive(vm, procedure)
            is CompoundProcedure  -> executeCompound(vm, procedure)
            else                  -> throw EvaluationException("invalid proceduce '$procedure'")
        }
    }

    private fun executePrimitive(vm: VM, procedure: PrimitiveProcedure) {
        vm.value = procedure.apply(vm.arg)
        vm.pc = vm.pop() as Int
    }

    private fun executeCompound(vm: VM, procedure: CompoundProcedure) {
        vm.env = procedure.env
        vm.pc = procedure.address
    }

    // Though the application itself will only modify PC, ENV or VAL, the called code could modify anything.
    override fun modifies(register: Register) = true

    override fun toString() = "(${if (tail) "tail-call" else "call"} ${Register.PROCEDURE} ${Register.ARG})"
}

