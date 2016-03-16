package komu.blunt.asm

class Linkage private constructor(private val name: String, val label: Label?) {

    companion object {
        val NEXT = Linkage("next", null);
        val RETURN = Linkage("return", null);
        fun jump(label: Label) = Linkage("jump", label)
    }

    override fun toString() =
        if (label != null)
            "$name $label"
        else
            name
}
