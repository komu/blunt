package komu.blunt.types

import java.util.Collections.emptyList
import komu.blunt.eval.TypeCheckException
import komu.blunt.types.checker.UnificationException
import komu.blunt.types.checker.Unifier
import komu.blunt.utils.addAll

class ClassEnv {

    private val classes = hashMap<String,TypeClass>()
    private val defaults = arrayList<Type>();

    {
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

        addInstance(arrayList(isIn("Ord", typeVariable("a")), isIn("Ord", typeVariable("b"))),
                    isIn("Ord", tupleType(typeVariable("a"), typeVariable("b"))))

        addInstance(arrayList(isIn("Eq", typeVariable("a")), isIn("Eq", typeVariable("b"))),
                    isIn("Eq", tupleType(typeVariable("a"), typeVariable("b"))))

        addInstance(arrayList(isIn("Eq", typeVariable("a"))),
                    isIn("Eq", listType(typeVariable("a"))))

        addInstance(arrayList(isIn("Eq", typeVariable("a"))),
                    isIn("Eq", genericType("Maybe", typeVariable("a"))))
    }

    public fun addInstance(predicate: Predicate) {
        addInstance(emptyList(), predicate)
    }

    public fun addInstance(predicates: List<Predicate>, predicate: Predicate) {
        val typeClass = getClass(predicate.className)

        if (predicate.overlapsAny(typeClass.instancePredicates))
            throw RuntimeException("overlapping instances")

        typeClass.addInstance(Qualified(predicates, predicate))
    }

    public fun addClass(name: String, vararg superClasses: String) {
        addClass(name, TypeClass(superClasses.toList()))
    }

    public fun addClass(name: String, cl: TypeClass) {
        if (defined(name))
            throw RuntimeException("class already defined: '$name'")

        for (superClass in cl.superClasses)
            if (!classes.containsKey(superClass))
                throw RuntimeException("unknown superclass '$superClass'")

        classes[name] = cl
    }

    private fun bySuper(predicate: Predicate): List<Predicate> {
        val typeClass = getClass(predicate.className)
        val result = listBuilder<Predicate>()
        result.add(predicate)

        for (superName in typeClass.superClasses)
            result.addAll(bySuper(isIn(superName, predicate.predicateType)))

        return result.build()
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
        val result = listBuilder<Predicate>()

        for (predicate in predicates) {
            if (predicate.inHnf) {
                result.add(predicate)
            } else {
                val qs = byInstance(predicate) ?: throw TypeCheckException("could not find instance of ${predicate.className} for type ${predicate.predicateType}")
                result.addAll(toHfns(qs))
            }
        }

        return result.build()
    }

    private fun simplify(ps: List<Predicate>): List<Predicate> {
        val combinedPredicates = hashSet<Predicate>()
        val result = listBuilder<Predicate>()

        for (p in ps) {
            if (!combinedPredicates.entails(p)) {
                combinedPredicates.add(p)
                result.add(p)
            }
        }

        return result.build()
    }

    // Returns true iff this collection of predicates entails "entailed".
    private fun Collection<Predicate>.entails(entailed: Predicate): Boolean {
        if (this.any { entailed in bySuper(it) })
            return true

        val qs = byInstance(entailed)
        return qs != null && qs.all { entails(it) }
    }

    fun reduce(ps: List<Predicate>): List<Predicate> =
        simplify(toHfns(ps))

    fun split(fixedVariables: Set<TypeVariable>,
              quantifyVariables: Set<TypeVariable>,
              originalPredicates: List<Predicate>): Pair<List<Predicate>, List<Predicate>> {
        val deferredPredicates = listBuilder<Predicate>()
        val retainedPredicates = listBuilder<Predicate>()

        for (predicate in reduce(originalPredicates))
            if (fixedVariables.containsAll(predicate.typeVariables))
                deferredPredicates.add(predicate)
            else
                retainedPredicates.add(predicate)

        // TODO: defaulted
        return Pair(deferredPredicates.build(), retainedPredicates.build())
    }

    fun defined(name: String) =
        classes.containsKey(name)

    private fun getClass(name: String): TypeClass =
        classes[name] ?: throw RuntimeException("unknown class '$name'");
}

