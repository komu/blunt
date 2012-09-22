package komu.blunt.stdlib

import komu.blunt.objects.TypeConstructorValue

fun booleanToConstructor(b: Boolean): TypeConstructorValue = if (b) BasicValues.TRUE else BasicValues.FALSE
/*
fun BigInteger.plus(rhs: BigInteger) = this.add(rhs)
fun BigInteger.minus(rhs: BigInteger) = this.subtract(rhs)
fun BigInteger.times(rhs: BigInteger) = this.multiply(rhs)
*/
