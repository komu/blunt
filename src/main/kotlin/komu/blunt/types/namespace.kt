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

//private static String mapName(Class<?> type) {
//return (type == Void.class)                             ? "Unit"
//: (type == Boolean.class || type == boolean.class) ? "Boolean"
//: (type == BigInteger.class)                       ? "Integer"
//: (type == String.class)                           ? "String"
//: type.getSimpleName();
//}

fun typeVariable(name: String): Type.Var =
    typeVariable(name, Kind.Star)

fun typeVariable(name: String, kind: Kind): Type.Var =
    Type.Var(name, kind)

fun listType(t: Type) =
    Type.App(Type.Con("[]", Kind.ofParams(1)), t)


fun basicType(name: String): Type =
    Type.Con(name, Kind.Star)

fun functionType(argumentType: Type, returnType: Type) =
    genericType("->", argumentType, returnType)

fun functionType(args: List<Type>, resultType: Type): Type =
    if (args.isEmpty())
        resultType
    else
        functionType(args.first(), functionType(args.drop(1), resultType))

fun tupleType(vararg types: Type): Type =
    tupleType(types.toList())

fun tupleType(types: List<Type>): Type =
    genericType(ConstructorNames.tupleName(types.size), types)

fun genericType(name: String, vararg params: Type): Type =
    genericType(name, params.toList())

fun genericType(name: String, params: List<Type>): Type {
    var t: Type = Type.Con(name, Kind.ofParams(params.size))

    for (param in params)
        t = Type.App(t, param)

    return t
}
