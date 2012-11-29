package komu.blunt.objects

import java.util.Arrays

class TypeConstructorValue(val index: Int, val name: String, val items: Array<Any?> = TypeConstructorValue.EMPTY_ARRAY) : Comparable<TypeConstructorValue> {

    {
        require(index >= 0, "invalid index: $index")
    }

    class object {
        private val EMPTY_ARRAY = Array<Any?>(0) { x -> null }
    }

    fun isTuple() = name.startsWith("(,")

    fun toString(): String =
        when {
            items.size == 0 -> name
            isTuple()       -> toStringAsTuple()
            name == ":"     -> toStringAsList()
            else            -> toStringDefault()
        }

    private fun toStringAsList(): String {
        val sb = StringBuilder()

        sb.append('[')

        sb.append(items[0])

        var value: TypeConstructorValue = items[1] as TypeConstructorValue
        while (value.name == ":") {
            sb.append(", ").append(value.items[0])
            value = value.items[1] as TypeConstructorValue
        }

        sb.append(']')

        return sb.toString()
    }

    private fun toStringDefault(): String {
        val sb = StringBuilder()

        sb.append('(').append(name)

        for (param in items)
            sb.append(' ').append(param)

        sb.append(')')
        return sb.toString()
    }

    private fun toStringAsTuple() =
        items.makeString(", ", "(", ")")

    fun equals(o: Any?) =
        o is TypeConstructorValue && index == o.index && Arrays.equals(items, o.items)

    fun hashCode() =
        Arrays.hashCode(items)

    override fun compareTo(other: TypeConstructorValue): Int {
//        if (index != o.index)
//            return index - o.index;
//
//        for (i in items.indices) {
//            val lhs = items[i] as Comparable<*>
//            val rhs = o.items[i] as Comparable<*>
//            val c = lhs.compareTo(rhs);
//            if (c != 0)
//                return c;
//        }
//        return 0;
        throw UnsupportedOperationException("comparison is not supported")
    }
}
