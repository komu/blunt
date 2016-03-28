package komu.blunt.objects

interface PrimitiveProcedure : Procedure {
    operator fun invoke(arg: Any?): Any?
}
