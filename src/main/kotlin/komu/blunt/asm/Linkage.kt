package komu.blunt.asm

class Linkage private(private val name: String, val label: Label?) {

    class object {
        val NEXT = Linkage("next", null);
        val RETURN = Linkage("return", null);
        fun jump(label: Label) = Linkage("jump", label)
    }

    fun toString() =
        if (label != null)
            "$name $label"
        else
            name
}
