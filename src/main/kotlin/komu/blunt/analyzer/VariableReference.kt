package komu.blunt.analyzer

import komu.blunt.objects.Symbol

data class VariableReference private constructor(val frame: Int, val offset: Int, val name: Symbol) {

    init {
        require(frame >= GLOBAL_FRAME) { "invalid frame $frame" }
        require(offset >= 0) { "invalid offset $offset" }
    }

    companion object {
        val GLOBAL_FRAME: Int = -1

        fun nested(frame: Int, offset: Int, name: Symbol) =
            VariableReference(frame, offset, name)
    }

    val global: Boolean
        get() = frame == GLOBAL_FRAME

    override fun toString() = if (global) "(GlobalVar $offset)" else "(LocalVar $frame:$offset)"
}
