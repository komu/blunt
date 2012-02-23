package komu.blunt.types

import komu.blunt.analyzer.AnalyzationException
import komu.blunt.ast.ASTDataDefinition

import java.util.*

import com.google.common.base.Strings.repeat
import java.util.Collections.unmodifiableCollection
import komu.blunt.types.quantifyAll
import komu.blunt.types.Type.*
import komu.blunt.parser.TypeParser

public class DataTypeDefinitions() {

    private val constructors = HashMap<String,ConstructorDefinition>();

    {
        register(0, ConstructorNames.UNIT.sure(), "()", 0)
        register(0, ConstructorNames.NIL.sure(), "[a]", 0)
        register(1, ConstructorNames.CONS.sure(), "a -> [a] -> [a]", 2)

        // TODO: create necessary tuples on demand
        for (val arity in 2..31)
            register(ConstructorDefinition(arity-2, ConstructorNames.tupleName(arity).sure(), tupleConstructorScheme(arity), arity));
    }

    private fun register(index: Int, name: String, scheme: String, arity: Int) {
        register(ConstructorDefinition(0, name, TypeParser.parseScheme(scheme), arity))
    }

    fun register(definition: ASTDataDefinition) {
        for (val constructor in definition.constructors)
            register(constructor.sure())
    }

    fun register(definition: ConstructorDefinition) {
        if (constructors.containsKey(definition.name))
            throw AnalyzationException("duplicate type constructor '${definition.name}'")

        constructors.put(definition.name.sure(), definition)
    }

    fun findConstructor(name: String): ConstructorDefinition {
        val ctor = constructors.get(name)
        if (ctor != null)
            return ctor
        else
            throw AnalyzationException("unknown type constructor '$name'")
    }

    private fun tupleConstructorScheme(arity: Int): Scheme {
        val types = ArrayList<Type>(arity)
        for (val i in 0..arity+1)
            types.add(Type.variable("t$i"))

        return quantifyAll(Qualified(Type.function(types, Type.tuple(types))))
    }

    fun getDeclaredConstructors(): Collection<ConstructorDefinition> =
        unmodifiableCollection(constructors.values()).sure()
}
