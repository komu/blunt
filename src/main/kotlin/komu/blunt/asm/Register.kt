package komu.blunt.asm

// TODO: enum
class Register private (private val name: String) {
    class object {
        val VAL       = Register("val")
        val ENV       = Register("env")
        val PROCEDURE = Register("procedure")
        val ARG       = Register("arg")
        val PC        = Register("pc")
    }

    fun toString() = name
}
