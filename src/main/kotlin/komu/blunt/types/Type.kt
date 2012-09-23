package komu.blunt.types

abstract class Type : Types<Type> {
    abstract fun instantiate(vars: List<TypeVariable>): Type
    public abstract val hnf: Boolean
    public abstract val kind: Kind

    fun toString() = toString(0)

    protected abstract fun toString(precedence: Int): String
}

