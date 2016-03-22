package komu.blunt.types

import java.util.Objects.hash

sealed class Kind {

    companion object {
        fun ofParams(count: Int): Kind {
            require(count >= 0) { "negative count: $count" }

            return if (count == 0) Star else Arrow(Star, Kind.ofParams(count - 1))
        }
    }

    override fun toString() = toString(false)

    protected abstract fun toString(isLeft: Boolean): String

    object Star : Kind() {
        override fun toString(isLeft: Boolean) = "*"
    }

    class Arrow(val left: Kind, val right: Kind) : Kind() {

        override fun equals(other: Any?) = other is Arrow && left == other.left && right == other.right
        override fun hashCode() = hash(left, right)

        override fun toString(isLeft: Boolean): String =
            if (isLeft)
                "(${left.toString(true)} -> ${right.toString(false)})"
            else
                "${left.toString(true)} -> ${right.toString(false)}"
    }
}

