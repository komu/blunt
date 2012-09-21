package komu.blunt.objects

import komu.blunt.eval.Environment

class CompoundProcedure(val address: Int, val env: Environment) : Procedure {

    {
        check(address >= 0, "negative address: $address")
    }

    fun toString() = "<#CompoundProcedure $address>"
}
