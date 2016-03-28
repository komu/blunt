package komu.blunt.asm

sealed class Linkage() {

    object Next : Linkage() {
        override fun toString() = "next"
    }

    object Return : Linkage() {
        override fun toString() = "return"
    }

    class Jump(val label: Label) : Linkage() {
        override fun toString() = "jump $label"
    }
}
