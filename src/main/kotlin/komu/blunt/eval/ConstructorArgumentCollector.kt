package komu.blunt.eval

import komu.blunt.objects.PrimitiveProcedure
import komu.blunt.objects.TypeConstructorValue
import komu.blunt.types.ConstructorDefinition

class ConstructorArgumentCollector(private val ctor: ConstructorDefinition,
                                   private val args: Array<Any?>) : PrimitiveProcedure {

    override fun invoke(arg: Any?): Any? {
        val newArgs = Array(args.size + 1) { if (it < args.size) args[it] else arg }
        return if (newArgs.size == ctor.arity)
            TypeConstructorValue(ctor.index, ctor.name, newArgs)
        else
            ConstructorArgumentCollector(ctor, newArgs)
    }
}

fun createConstructor(ctor: ConstructorDefinition): Any =
    if (ctor.arity == 0)
        TypeConstructorValue(ctor.index, ctor.name)
    else
        ConstructorArgumentCollector(ctor, emptyArray())
