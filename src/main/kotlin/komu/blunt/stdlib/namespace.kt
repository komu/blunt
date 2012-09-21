package komu.blunt.stdlib

import java.math.BigInteger
import komu.blunt.objects.TypeConstructorValue

fun booleanToConstructor(b: Boolean): TypeConstructorValue = if (b) BasicValues.TRUE else BasicValues.FALSE

fun BigInteger.plus(rhs: BigInteger) = this.add(rhs)
