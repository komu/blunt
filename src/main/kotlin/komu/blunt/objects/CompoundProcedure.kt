package komu.blunt.objects

import komu.blunt.eval.Environment

class CompoundProcedure(val address: Int, val env: Environment) : Procedure {

    init {
        require(address >= 0) { "negative address: $address" }
    }

    override fun toString() = "<#CompoundProcedure $address>"
}
