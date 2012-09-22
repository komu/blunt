package komu.blunt.analyzer

import komu.blunt.objects.Symbol

class VariableReference private (val frame: Int, val offset: Int, val name: Symbol) {

    {
        check(frame >= GLOBAL_FRAME, "invalid frame $frame")
        check(offset >= 0, "invalid offset $offset")
    }

    class object {
        public val GLOBAL_FRAME: Int = -1

        fun nested(frame: Int, offset: Int, name: Symbol) =
            VariableReference(frame, offset, name)

        fun global(offset: Int, name: Symbol) =
            VariableReference(GLOBAL_FRAME, offset, name)
    }

    val global: Boolean
        get() = frame == GLOBAL_FRAME

    fun toString() = if (global) "(GlobalVar $offset)" else "(LocalVar $frame:$offset)"
}
