package komu.blunt.types

import java.util.Objects.hash

abstract class Kind protected constructor() {

    companion object {
        val STAR: Kind = StarKind()

        fun arrow(left: Kind, right: Kind): Kind = ArrowKind(left, right)

        fun ofParams(count: Int): Kind {
            if (count < 0) throw IllegalArgumentException("negative count: $count")

            return if (count == 0) STAR else arrow(STAR, Kind.ofParams(count-1))
        }
    }

    override fun toString() = toString(false)

    protected abstract fun toString(l: Boolean): String
}

class StarKind : Kind() {
    override fun toString(l: Boolean) = "*"
}

class ArrowKind(val left: Kind, val right: Kind) : Kind() {

    override fun equals(other: Any?) = other is ArrowKind && left == other.left && right == other.right
    override fun hashCode() = hash(left, right)

    override fun toString(l: Boolean): String =
        if (l)
            "(" + left.toString(true) + " -> " + right.toString(false) + ")"
        else
            left.toString(true) + " -> " + right.toString(false)
}
