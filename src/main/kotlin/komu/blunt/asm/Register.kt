package komu.blunt.asm

import komu.blunt.eval.Environment
import komu.blunt.objects.Procedure

enum class Register (val name: String) {
    VAL       : Register("val")
    ENV       : Register("env")
    PROCEDURE : Register("procedure")
    ARG       : Register("arg")
    PC        : Register("pc")

    fun isValidValue(value: Any): Boolean =
        when (this) {
            PROCEDURE -> value is Procedure
            PC        -> value is Int
            ENV       -> value is Environment
            else      -> true
        }

    fun toString() = name
}
