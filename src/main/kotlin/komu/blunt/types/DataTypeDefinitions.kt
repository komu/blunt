package komu.blunt.types

import java.util.*
import komu.blunt.analyzer.AnalyzationException
import komu.blunt.ast.ASTDataDefinition
import komu.blunt.parser.TypeParser

public class DataTypeDefinitions() {

    private val constructors = HashMap<String,ConstructorDefinition>();

    {
        register(0, ConstructorNames.UNIT, "()", 0)
        register(0, ConstructorNames.NIL, "[a]", 0)
        register(1, ConstructorNames.CONS, "a -> [a] -> [a]", 2)

        // TODO: create necessary tuples on demand
        for (val arity in 2..31)
            register(ConstructorDefinition(arity-2, ConstructorNames.tupleName(arity), tupleConstructorScheme(arity), arity))
    }

    private fun register(index: Int, name: String, scheme: String, arity: Int) {
        register(ConstructorDefinition(index, name, TypeParser.parseScheme(scheme), arity))
    }

    fun register(definition: ASTDataDefinition) {
        for (val constructor in definition.constructors)
            register(constructor)
    }

    fun register(definition: ConstructorDefinition) {
        if (constructors.containsKey(definition.name))
            throw AnalyzationException("duplicate type constructor '${definition.name}'")

        constructors[definition.name] = definition
    }

    fun findConstructor(name: String): ConstructorDefinition =
        constructors[name] ?: throw AnalyzationException("unknown type constructor '$name'")

    private fun tupleConstructorScheme(arity: Int): Scheme {
        val types = (0..arity-1).map { typeVariable("t$it")}
        return Qualified.simple(functionType(types, tupleType(types))).quantifyAll()
    }

    val declaredConstructors: Collection<ConstructorDefinition>
        get() = constructors.values()
}
