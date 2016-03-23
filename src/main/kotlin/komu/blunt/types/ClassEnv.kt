package komu.blunt.types

import komu.blunt.eval.TypeCheckException
import komu.blunt.types.checker.UnificationException
import komu.blunt.types.checker.Unifier
import java.util.*
import java.util.Collections.emptyList

class ClassEnv {

    private val classes = HashMap<String,TypeClass>()
    private val defaults = ArrayList<Type>();

    init {
        defaults.add(BasicType.INTEGER)

        addClass("Eq")
        addClass("Ord", "Eq")
        addClass("Show")
        addClass("Read")
        addClass("Bounded")
        addClass("Enum")
        addClass("Functor")
        addClass("Monad")

        addClass("Num", "Eq", "Show")
        addClass("Real", "Num", "Ord")
        addClass("Fractional", "Num")
        addClass("Integral", "Real", "Enum")
        addClass("RealFrac", "Real", "Fractional")
        addClass("Floating", "Fractional")
        addClass("RealFloat", "RealFrac", "Floating")

        addDefaultInstances()
    }

    private fun addDefaultInstances() {
        addInstance(isIn("Num", BasicType.INTEGER))
        addInstance(isIn("Eq", BasicType.INTEGER))
        addInstance(isIn("Eq", BasicType.STRING))
        addInstance(isIn("Eq", BasicType.UNIT))

        addInstance(isIn("Ord", BasicType.UNIT))
        addInstance(isIn("Ord", BasicType.INTEGER))
        addInstance(isIn("Ord", BasicType.STRING))

        addInstance(listOf(isIn("Ord", Type.Var("a")), isIn("Ord", Type.Var("b"))),
                    isIn("Ord", Type.tuple(Type.Var("a"), Type.Var("b"))))

        addInstance(listOf(isIn("Eq", Type.Var("a")), isIn("Eq", Type.Var("b"))),
                    isIn("Eq", Type.tuple(Type.Var("a"), Type.Var("b"))))

        addInstance(listOf(isIn("Eq", Type.Var("a"))),
                    isIn("Eq", Type.list(Type.Var("a"))))

        addInstance(listOf(isIn("Eq", Type.Var("a"))),
                    isIn("Eq", Type.generic("Maybe", Type.Var("a"))))
    }

    fun addInstance(predicate: Predicate) {
        addInstance(emptyList(), predicate)
    }

    fun addInstance(predicates: List<Predicate>, predicate: Predicate) {
        val typeClass = getClass(predicate.className)

        if (predicate.overlapsAny(typeClass.instancePredicates))
            throw RuntimeException("overlapping instances")

        typeClass.addInstance(Qualified(predicates, predicate))
    }

    fun addClass(name: String, vararg superClasses: String) {
        addClass(name, TypeClass(superClasses.asList()))
    }

    fun addClass(name: String, cl: TypeClass) {
        if (defined(name))
            throw RuntimeException("class already defined: '$name'")

        for (superClass in cl.superClasses)
            if (!classes.containsKey(superClass))
                throw RuntimeException("unknown superclass '$superClass'")

        classes[name] = cl
    }

    private fun bySuper(predicate: Predicate): List<Predicate> {
        val typeClass = getClass(predicate.className)
        val result = ArrayList<Predicate>()
        result.add(predicate)

        for (superName in typeClass.superClasses)
            result += bySuper(isIn(superName, predicate.predicateType))

        return result
    }

    private fun byInstance(predicate: Predicate): List<Predicate>? {
        val typeClass = getClass(predicate.className)
        for (instance in typeClass.instances) {
            try {
                val s = Unifier.matchPredicate(instance.predicate, predicate)
                return instance.predicates.map { it.apply(s) }
            } catch (e: UnificationException) {
                // skip
            }
        }

        return null;
    }

    private fun toHfns(predicates: List<Predicate>): List<Predicate> {
        val result = ArrayList<Predicate>()

        for (predicate in predicates) {
            if (predicate.inHnf) {
                result += predicate
            } else {
                val qs = byInstance(predicate) ?: throw TypeCheckException("could not find instance of ${predicate.className} for type ${predicate.predicateType}")
                result += toHfns(qs)
            }
        }

        return result
    }

    private fun simplify(ps: List<Predicate>): List<Predicate> {
        val combinedPredicates = HashSet<Predicate>()
        val result = ArrayList<Predicate>()

        for (p in ps) {
            if (!combinedPredicates.entails(p)) {
                combinedPredicates += p
                result += p
            }
        }

        return result
    }

    // Returns true iff this collection of predicates entails "entailed".
    private fun Collection<Predicate>.entails(entailed: Predicate): Boolean =
            this.any { entailed in bySuper(it) } || byInstance(entailed)?.all { entails(it) } ?: false

    fun reduce(ps: List<Predicate>): List<Predicate> =
        simplify(toHfns(ps))

    fun split(fixedVariables: Set<Type.Var>,
              quantifyVariables: Set<Type.Var>,
              originalPredicates: List<Predicate>): Pair<List<Predicate>, List<Predicate>> {
        val deferredPredicates = ArrayList<Predicate>()
        val retainedPredicates = ArrayList<Predicate>()

        for (predicate in reduce(originalPredicates))
            if (predicate.typeVars().all { it in fixedVariables })
                deferredPredicates.add(predicate)
            else
                retainedPredicates.add(predicate)

        // TODO: defaulted
        return Pair(deferredPredicates, retainedPredicates)
    }

    fun defined(name: String) =
        name in classes

    private fun getClass(name: String): TypeClass =
        classes[name] ?: throw RuntimeException("unknown class '$name'");
}

