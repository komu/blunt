package komu.blunt.types

import java.math.BigInteger

object BasicType {
    //val UNIT = basicType("Unit")
    public val UNIT: Type = basicType("Unit")
    val INTEGER = basicType("Integer")
    val STRING = basicType("String")

    private fun basicType(name: String): Type =
        TypeConstructor(name, Kind.STAR)
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

fun typeVariable(name: String): TypeVariable =
    typeVariable(name, Kind.STAR)

fun typeVariable(name: String, kind: Kind): TypeVariable =
    TypeVariable(name, kind)

fun listType(t: Type) =
    TypeApplication(TypeConstructor("[]", Kind.ofParams(1)), t)


fun basicType(name: String): Type =
    TypeConstructor(name, Kind.STAR)

fun functionType(argumentType: Type, returnType: Type) =
    genericType("->", argumentType, returnType)

fun functionType(args: List<Type>, resultType: Type): Type =
    if (args.empty)
        resultType
    else
        functionType(args.first(), functionType(args.tail, resultType))

fun tupleType(vararg types: Type): Type =
    tupleType(types.toList())

fun tupleType(types: List<Type>): Type =
    genericType(ConstructorNames.tupleName(types.size), types)

fun genericType(name: String, vararg params: Type): Type =
    genericType(name, params.toList())

fun genericType(name: String, params: List<Type>): Type {
    var t: Type = TypeConstructor(name, Kind.ofParams(params.size))

    for (param in params)
        t = TypeApplication(t, param)

    return t
}
