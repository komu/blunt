package komu.blunt.types

import std.util.*
import komu.blunt.types.checker.Substitution

import java.util.List
import java.util.Set
import java.util.LinkedHashSet

abstract class Type : Types<Type> {
    class object {
        val UNIT = basic("Unit")
        val BOOLEAN = basic("Boolean")
        val INTEGER = basic("Integer")
        val STRING = basic("String")

        fun fromClass(cl: Class<*>) = basic(mapName(cl))

        fun fromObject(o: Any?): Type {
            throw UnsupportedOperationException()
            //return fromClass(o.getClass());
        }

        private fun mapName(cl: Class<*>): String {
            return cl.getSimpleName().sure()
            /*
            return (type == Void.class)                             ? "Unit"
            : (type == Boolean.class || type == boolean.class) ? "Boolean"
            : (type == BigInteger.class)                       ? "Integer"
            : (type == String.class)                           ? "String"
            : type.getSimpleName();
            */
        }

        fun variable(name: String) = variable(name, Kind.STAR.sure())
        fun variable(name: String, kind: Kind) = TypeVariable(name, kind)

        fun list(t: Type) = TypeApplication(TypeConstructor("[]", Kind.ofParams(1).sure()), t)

        fun basic(name: String) = TypeConstructor(name, Kind.STAR.sure())

        fun function(argumentType: Type, returnType: Type): Type =
            generic("->", argumentType, returnType)

        fun function(args: List<Type>, resultType: Type): Type =
            if (args.isEmpty())
                resultType
            else
                function(args.get(0), function(args.subList(1, args.size()).sure(), resultType))

        fun generic(name: String, vararg params: Type): Type =
            generic(name, params.toList())

        fun generic(name: String, params: List<out Type>): Type {
            var t: Type = TypeConstructor(name, Kind.ofParams(params.size()).sure())

            for (val param in params)
                t = TypeApplication(t, param)

            return t
        }

        fun tuple(vararg types: Type): Type =
            tuple(types.toList())

        fun tuple(types: List<Type>): Type =
            generic(ConstructorNames.tupleName(types.size()).sure(), types)
    }

    protected abstract fun instantiate(vars: List<TypeVariable>): Type
    protected abstract fun getKind(): Kind?
    protected abstract fun hnf(): Boolean
    protected abstract fun toString(precedence: Int): String

    fun toString() = toString(0)

    protected fun getTypeVariables(): Set<TypeVariable> {
        val vars = LinkedHashSet<TypeVariable>()
        addTypeVariables(vars)
        return vars
    }

    fun containsVariable(v: TypeVariable) =
        getTypeVariables().contains(v)
}

class TypeGen(private val index: Int) : Type() {

    override fun apply(s: Substitution) = this
    override fun instantiate(vars: List<TypeVariable>) = vars[index]
    override fun addTypeVariables(result: Set<TypeVariable>) { }
    override fun getKind() = throw RuntimeException("can't access kind of TypeGen")
    override fun hnf() = throw RuntimeException("should not call hnf for TypeGen")
    override fun toString(precedence: Int) = "TypeGen[$index]"
}

class TypeApplication(val left: Type, val right: Type) : Type() {
    override fun hnf(): Boolean = false
    override fun toString(precedence: Int): String = ""
    override fun instantiate(vars: List<TypeVariable>): Type = throw UnsupportedOperationException()
    override fun getKind(): Kind? = null
    override fun addTypeVariables(variables: Set<TypeVariable>) {
        throw UnsupportedOperationException()
    }
    override fun apply(substitution: Substitution): Type = throw UnsupportedOperationException()

    //
    //@Override
    //public TypeApplication apply(Substitution substitution) {
    //return new TypeApplication(left.apply(substitution), right.apply(substitution));
    //}
    //
    //@Override
    //protected Type instantiate(List<TypeVariable> vars) {
    //return new TypeApplication(left.instantiate(vars), right.instantiate(vars));
    //}
    //
    //@Override
    //public boolean hnf() {
    //return left.hnf();
    //}
    //
    //@Override
    //public void addTypeVariables(Set<TypeVariable> result) {
    //left.addTypeVariables(result);
    //right.addTypeVariables(result);
    //}
    //
    //@Override
    //public Kind getKind() {
    //Kind kind = left.getKind();
    //if (kind instanceof ArrowKind)
    //return ((ArrowKind) kind).right;
    //else
    //throw new TypeCheckException("invalid kind: " + left);
    //}
    //
    //@Override
    //protected String toString(int precedence) {
    //return toString(new LinkedList<Type>(), precedence);
    //}
    //
    //private String toString(LinkedList<Type> arguments, int precedence) {
    //arguments.addFirst(right);
    //if (left instanceof TypeApplication) {
    //return ((TypeApplication) left).toString(arguments, precedence);
    //
    //} else if (left instanceof TypeConstructor) {
    //TypeConstructor ct = (TypeConstructor) left;
    //return ct.toString(arguments, precedence);
    //
    //} else {
    //return "(" + left + " " + arguments + ")";
    //}
    //}
    //
    //@Override
    //public boolean equals(Object obj) {
    //if (obj == this) return true;
    //
    //if (obj instanceof TypeApplication) {
    //TypeApplication rhs = (TypeApplication) obj;
    //
    //return left.equals(rhs.left)
    //&& right.equals(rhs.right);
    //}
    //
    //return false;
    //}
    //
    //@Override
    //public int hashCode() {
    //return left.hashCode() * 79 + right.hashCode();
    //}
}

class TypeConstructor(private val name: String, private val kind: Kind) : Type() {
    override fun toString(precedence: Int): String = ""
    override fun instantiate(vars: List<TypeVariable>) = throw UnsupportedOperationException()
    override fun hnf(): Boolean = false
    override fun getKind(): Kind? = null
    override fun apply(substitution: Substitution) = throw UnsupportedOperationException()
    override fun addTypeVariables(variables: Set<TypeVariable>) {
        throw UnsupportedOperationException()
    }
    //
    //@Override
    //public TypeConstructor apply(Substitution substitution) {
    //return this;
    //}
    //
    //@Override
    //public boolean hnf() {
    //return false;
    //}
    //
    //@Override
    //protected Type instantiate(List<TypeVariable> vars) {
    //return this;
    //}
    //
    //@Override
    //public void addTypeVariables(Set<TypeVariable> result) {
    //}
    //
    //@Override
    //public Kind getKind() {
    //return kind;
    //}
    //
    //@Override
    //protected String toString(final int precedence) {
    //return name;
    //}
    //
    //@Override
    //public boolean equals(Object obj) {
    //if (obj == this) return true;
    //
    //if (obj instanceof TypeConstructor) {
    //TypeConstructor rhs = (TypeConstructor) obj;
    //
    //return name.equals(rhs.name)
    //&& kind.equals(rhs.kind);
    //}
    //
    //return false;
    //}
    //
    //@Override
    //public int hashCode() {
    //return name.hashCode() * 79 + kind.hashCode();
    //}
    //
    //String toString(List<Type> arguments, int precedence) {
    //if (name.equals("->") && arguments.size() == 2) {
    //return functionToString(arguments, precedence);
    //
    //} else if (name.equals("[]") && arguments.size() == 1) {
    //return "[" + arguments.get(0) + "]";
    //
    //} else if (name.matches("\\(,+\\)")) {
    //return tupleToString(arguments);
    //
    //} else {
    //return defaultToString(arguments, precedence);
    //}
    //}
    //
    //private String defaultToString(List<Type> arguments, int precedence) {
    //StringBuilder sb = new StringBuilder();
    //
    //if (precedence != 0) sb.append("(");
    //sb.append(name);
    //
    //for (Type arg : arguments)
    //sb.append(' ').append(arg.toString(1));
    //
    //if (precedence != 0) sb.append(")");
    //
    //return sb.toString();
    //}
    //
    //private String tupleToString(List<Type> arguments) {
    //StringBuilder sb = new StringBuilder();
    //sb.append("(");
    //for (Iterator<Type> iterator = arguments.iterator(); iterator.hasNext(); ) {
    //sb.append(iterator.next().toString(0));
    //if (iterator.hasNext())
    //sb.append(", ");
    //}
    //sb.append(")");
    //return sb.toString();
    //}
    //
    //private String functionToString(List<Type> arguments, int precedence) {
    //StringBuilder sb = new StringBuilder();
    //
    //if (precedence != 0) sb.append("(");
    //sb.append(arguments.get(0).toString(1)).append(" -> ").append(arguments.get(1).toString(0));
    //if (precedence != 0) sb.append(")");
    //
    //return sb.toString();
    //}

}

class TypeVariable(private val name: String, private val kind: Kind) : Type() {
    override fun toString(precedence: Int): String = ""
    override fun instantiate(vars: List<TypeVariable>) = throw UnsupportedOperationException()
    override fun hnf(): Boolean = false
    override fun getKind(): Kind? = null
    override fun apply(substitution: Substitution) = throw UnsupportedOperationException()
    override fun addTypeVariables(variables: Set<TypeVariable>) {
        throw UnsupportedOperationException()
    }

    //
    //@Override
    //public boolean hnf() {
    //return true;
    //}
    //
    //@Override
    //protected String toString(final int precedence) {
    //return name;
    //}
    //
    //@Override
    //public Kind getKind() {
    //return kind;
    //}
    //
    //@Override
    //public Type apply(Substitution substitution) {
    //Type newType = substitution.lookup(this);
    //return newType != null ? newType : this;
    //}
    //
    //@Override
    //protected Type instantiate(List<TypeVariable> vars) {
    //return this;
    //}
    //
    //@Override
    //public void addTypeVariables(Set<TypeVariable> result) {
    //result.add(this);
    //}
    //
    //@Override
    //public boolean equals(Object obj) {
    //if (obj == this) return true;
    //
    //if (obj instanceof TypeVariable) {
    //TypeVariable rhs = (TypeVariable) obj;
    //
    //return name.equals(rhs.name)
    //&& kind.equals(rhs.kind);
    //}
    //
    //return false;
    //}
    //
    //@Override
    //public int hashCode() {
    //return Objects.hashCode(name, kind);
    //}
}

