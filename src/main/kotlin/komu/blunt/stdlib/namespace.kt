package komu.blunt.stdlib

import komu.blunt.objects.TypeConstructorValue

fun booleanToConstructor(b: Boolean): TypeConstructorValue = if (b) BasicValues.TRUE else BasicValues.FALSE
