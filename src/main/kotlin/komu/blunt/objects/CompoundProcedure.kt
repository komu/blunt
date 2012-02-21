package komu.blunt.objects

import komu.blunt.eval.Environment

class CompoundProcedure(val address: Int, val env: Environment) : Procedure {

    {
        if (address < 0) throw IllegalArgumentException("negative address: $address")
    }

    override fun toString() = "<#CompoundProcedure $address>"
}
