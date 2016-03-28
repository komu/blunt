package komu.blunt.objects

class PrimitiveFunction(val func: (Any?) -> Any?) : PrimitiveProcedure {
    override fun invoke(arg: Any?): Any? = func(arg)
}

