package komu.blunt.types

import kotlin.util.*
import java.util.List

object BasicType {
    val UNIT = basicType("Unit");
    val INTEGER = basicType("Integer");
    val STRING = basicType("String");

    private fun basicType(name: String): Type =
        TypeConstructor(name, Kind.STAR.sure())
}

fun typeFromObject(o: Any): Type =
    throw UnsupportedOperationException()

//private static String mapName(Class<?> type) {
//return (type == Void.class)                             ? "Unit"
//: (type == Boolean.class || type == boolean.class) ? "Boolean"
//: (type == BigInteger.class)                       ? "Integer"
//: (type == String.class)                           ? "String"
//: type.getSimpleName();
//}

fun typeVariable(name: String): TypeVariable =
    typeVariable(name, Kind.STAR.sure())

fun typeVariable(name: String, kind: Kind): TypeVariable =
    TypeVariable(name, kind)

fun listType(t: Type) =
    TypeApplication(TypeConstructor("[]", Kind.ofParams(1).sure()), t)


fun basicType(name: String): Type =
    TypeConstructor(name, Kind.STAR.sure())

fun functionType(argumentType: Type, returnType: Type) =
    genericType("->", argumentType, returnType)

fun functionType(args: List<Type>, resultType: Type): Type =
    if (args.isEmpty())
        resultType
    else
        functionType(args.first(), functionType(args.subList(1, args.size).sure(), resultType))

fun tupleType(vararg types: Type): Type =
    tupleType(types.toList())

fun tupleType(types: List<Type>): Type =
    genericType(ConstructorNames.tupleName(types.size).sure(), types)

fun genericType(name: String, vararg params: Type): Type =
    genericType(name, params.toList())

fun genericType(name: String, params: List<out Type>): Type {
    var t: Type = TypeConstructor(name, Kind.ofParams(params.size))

    for (val param in params)
        t = TypeApplication(t, param)

    return t
}
