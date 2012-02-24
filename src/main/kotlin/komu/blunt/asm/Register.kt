package komu.blunt.asm

import komu.blunt.objects.Procedure
import komu.blunt.eval.Environment

// TODO: enum
class Register private (private val name: String) {
    class object {
        val VAL       = Register("val")
        val ENV       = Register("env")
        val PROCEDURE = Register("procedure")
        val ARG       = Register("arg")
        val PC        = Register("pc")
    }

    fun isValidValue(value: Any): Boolean =
        when (this) {
            Register.PROCEDURE -> value is Procedure
            Register.PC        -> value is Int
            Register.ENV       -> value is Environment
            else               -> true
        }

    fun toString() = name
}
