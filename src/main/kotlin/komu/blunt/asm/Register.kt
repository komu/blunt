package komu.blunt.asm

import komu.blunt.eval.Environment
import komu.blunt.objects.Procedure

enum class Register (val name_: String) {
    VAL("val"),
    ENV("env"),
    PROCEDURE("procedure"),
    ARG("arg"),
    PC("pc");

    fun isValidValue(value: Any): Boolean =
        when (this) {
            PROCEDURE -> value is Procedure
            PC        -> value is Int
            ENV       -> value is Environment
            else      -> true
        }

    override fun toString() = name_
}
