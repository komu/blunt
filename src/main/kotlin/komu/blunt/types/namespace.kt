package komu.blunt.types

import java.math.BigInteger

object BasicType {
    val UNIT = basicType("Unit")
    val INTEGER = basicType("Integer")
    val STRING = basicType("String")

    private fun basicType(name: String): Type =
        Type.Con(name, Kind.Star)
}

fun typeFromObject(o: Any): Type =
    when (o) {
        is BigInteger -> BasicType.INTEGER
        is String     -> BasicType.STRING
        else          -> throw UnsupportedOperationException()
    }

