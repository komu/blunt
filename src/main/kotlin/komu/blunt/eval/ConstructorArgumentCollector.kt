package komu.blunt.eval

import java.util.ArrayList
import komu.blunt.objects.PrimitiveProcedure
import komu.blunt.objects.TypeConstructorValue
import komu.blunt.types.ConstructorDefinition

class ConstructorArgumentCollector(private val ctor: ConstructorDefinition,
                                   private val args: List<Any?>) : PrimitiveProcedure {

    class object {
        fun createConstructor(ctor: ConstructorDefinition): Any =
            if (ctor.arity == 0)
                TypeConstructorValue(ctor.index, ctor.name)
            else
                ConstructorArgumentCollector(ctor, ArrayList<Any?>())
    }

    override fun apply(arg: Any?): Any? {
        val newArgs = ArrayList<Any?>(args.size + 1)
        newArgs.addAll(args)
        newArgs.add(arg)
        if (newArgs.size() == ctor.arity)
            return TypeConstructorValue(ctor.index, ctor.name, newArgs.toArray())
        else
            return ConstructorArgumentCollector(ctor, newArgs)
    }
}

