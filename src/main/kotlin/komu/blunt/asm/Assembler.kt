package komu.blunt.asm

class Assembler {
    private var labelCounter = 0
    fun newLabel(prefix: String) = Label("$prefix-${labelCounter++}")
}
