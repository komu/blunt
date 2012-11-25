package komu.blunt.eval

import komu.blunt.objects.PrimitiveProcedure
import komu.blunt.objects.TypeConstructorValue
import komu.blunt.types.ConstructorDefinition

class ConstructorArgumentCollector(private val ctor: ConstructorDefinition,
                                   private val args: Array<Any?>) : PrimitiveProcedure {

    class object {
        fun createConstructor(ctor: ConstructorDefinition): Any =
            if (ctor.arity == 0)
                TypeConstructorValue(ctor.index, ctor.name)
            else
                ConstructorArgumentCollector(ctor, array<Any?>())
    }

    override fun apply(arg: Any?): Any? {
        val newArgs = Array<Any?>(args.size+1) { i -> if (i < args.size) args[i] else arg }
        if (newArgs.size == ctor.arity)
            return TypeConstructorValue(ctor.index, ctor.name, newArgs)
        else
            return ConstructorArgumentCollector(ctor, newArgs)
    }
}

