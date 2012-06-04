package komu.blunt.analyzer

import komu.blunt.objects.Symbol

class VariableReference private (val frame: Int, val offset: Int, val name: Symbol) {

    {
        if (frame < GLOBAL_FRAME) throw IllegalArgumentException("invalid frame $frame")
        if (offset < 0) throw IllegalArgumentException("invalid offset $offset")
    }

    class object {
        public val GLOBAL_FRAME: Int = -1

        fun nested(frame: Int, offset: Int, name: Symbol) =
            VariableReference(frame, offset, name)

        fun global(offset: Int, name: Symbol) =
            VariableReference(GLOBAL_FRAME, offset, name)
    }

    fun isGlobal() = frame == GLOBAL_FRAME
    fun toString() = "VariableReference [frame=$frame, offset=$offset]"
}
