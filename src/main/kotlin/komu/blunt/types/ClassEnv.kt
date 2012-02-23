package komu.blunt.types

import std.util.*

import komu.blunt.eval.TypeCheckException
import komu.blunt.types.checker.Substitution
import komu.blunt.types.checker.UnificationException
import komu.blunt.types.checker.Unifier
import komu.blunt.utils.Pair

import javax.annotation.Nullable
import java.util.ArrayList
import java.util.HashMap
import java.util.Arrays.asList
import komu.blunt.types.isIn
import komu.blunt.types.Type.*
import java.util.Collection
import java.util.Collections
import java.util.List
import java.util.Set
import java.util.Arrays
import java.util.HashSet
import java.util.LinkedHashSet

class ClassEnv() {

    private val classes = HashMap<String,TypeClass>()
    private val defaults = ArrayList<Type>();

    {
        defaults.add(Type.INTEGER.sure())
        addCoreClasses();
        addNumClasses();
        addDefaultInstances();
    }

    private fun getSuperClasses(name: String): Collection<String> =
        getClass(name).superClasses

    private fun getInstances(name: String): Collection<ClassInstance> =
        getClass(name).instances

    private fun addCoreClasses() {
        addClass("Eq");
        addClass("Ord", "Eq");
        addClass("Show");
        addClass("Read");
        addClass("Bounded");
        addClass("Enum");
        addClass("Functor");
        addClass("Monad");
    }

    private fun addNumClasses() {
        addClass("Num", "Eq", "Show");
        addClass("Real", "Num", "Ord");
        addClass("Fractional", "Num");
        addClass("Integral", "Real", "Enum");
        addClass("RealFrac", "Real", "Fractional");
        addClass("Floating", "Fractional");
        addClass("RealFloat", "RealFrac", "Floating");
    }

    private fun addDefaultInstances() {
        addInstance(isIn("Num", Type.INTEGER));
        addInstance(isIn("Eq", Type.INTEGER));
        addInstance(isIn("Eq", Type.STRING));
        addInstance(isIn("Eq", Type.UNIT));

        addInstance(isIn("Ord", Type.UNIT));
        addInstance(isIn("Ord", Type.INTEGER));
        addInstance(isIn("Ord", Type.STRING));

        addInstance(asList(isIn("Ord", Type.variable("a")),
        isIn("Ord", Type.variable("b"))),
        isIn("Ord", Type.tuple(Type.variable("a"), Type.variable("b"))));

        addInstance(asList(isIn("Eq", Type.variable("a")),
        isIn("Eq", Type.variable("b"))),
        isIn("Eq", Type.tuple(Type.variable("a"), Type.variable("b"))));

        addInstance(asList(isIn("Eq", Type.variable("a"))),
        isIn("Eq", Type.list(Type.variable("a"))));

        addInstance(asList(isIn("Eq", Type.variable("a"))),
        isIn("Eq", Type.generic("Maybe", Type.variable("a"))));
    }

    public fun addInstance(predicate: Predicate) {
        addInstance(Collections.emptyList<Predicate>().sure(), predicate);
    }

    public fun addInstance(predicates: List<Predicate>?, predicate0: Predicate) {
        val predicate = predicate0.sure()
        val cl = getClass(predicate.className)

        if (predicate.overlapsAny(cl.instancePredicates()))
            throw RuntimeException("overlapping instances")

        cl.addInstance(Qualified(predicates.sure(), predicate))
    }

    public fun addClass(name: String, vararg superClasses: String) {
        addClass(name, TypeClass(superClasses.toList()))
    }

    public fun addClass(name: String, cl: TypeClass) {
        if (defined(name))
            throw RuntimeException("class already defined: '$name'")

        for (val superClass in cl.superClasses)
            if (classes.containsKey(superClass))
                throw RuntimeException("unknown superclass '$superClass'")

        classes.put(name, cl)
    }

    private fun bySuper(predicate: Predicate): List<Predicate> {
        val result = ArrayList<Predicate>()
        result.add(predicate)

        for (val superName in getSuperClasses(predicate.className))
            result.addAll(bySuper(isIn(superName, predicate.`type`).sure()))

        return result
    }

    private fun byInstance(predicate: Predicate): List<Predicate>? {
        for (val it2 in getInstances(predicate.className)) {
            val it = it2.sure()
            try {
                val s = Unifier.matchPredicate(it.predicate, predicate)
                return TypeUtils.applySubstitution(s, it.predicates)
            } catch (e: UnificationException) {
                // skip
            }
        }

        return null;
    }

    private fun entails(ps: Collection<Predicate>, p: Predicate): Boolean {
        for (val pp in ps)
           if (bySuper(pp).contains(p))
               return true

        val qs = byInstance(p)
        if (qs == null) {
            return false
        } else {
            for (val q in qs)
                if (!entails(ps, q.sure()))
                    return false
            return true
        }
    }

    private fun toHfns(predicates: List<Predicate>): List<Predicate> {
        val result = ArrayList<Predicate>()

        for (val predicate in predicates) {
            if (predicate.inHnf()) {
                result.add(predicate)
            } else {
                val qs = byInstance(predicate)
                if (qs != null)
                    result.addAll(toHfns(qs))
                else
                    throw TypeCheckException("could not find instance of ${predicate.className} for type ${predicate.`type`}")
            }
        }

        return result
    }

    private fun simplify(ps: List<Predicate>): List<Predicate> {
        val combinedPredicates = HashSet<Predicate>()
        val rs = ArrayList<Predicate>()

        for (val p in ps) {
            if (!entails(combinedPredicates, p)) {
                combinedPredicates.add(p)
                rs.add(p)
            }
        }

        return rs
    }

    fun reduce(ps: List<Predicate>): List<Predicate> =
        simplify(toHfns(ps))

    fun split(fixedVariables: Set<TypeVariable>,
              quantifyVariables: Set<TypeVariable>,
              originalPredicates: List<Predicate>): Pair<List<Predicate>?, List<Predicate>?> {
        val deferredPredicates = ArrayList<Predicate>()
        val retainedPredicates = ArrayList<Predicate>()

        for (val predicate in reduce(originalPredicates))
            if (fixedVariables.containsAll(getTypeVariables(predicate)))
                deferredPredicates.add(predicate)
            else
                retainedPredicates.add(predicate)

        // TODO: defaulted
        return Pair(deferredPredicates, retainedPredicates)
    }

    // TODO: duplication
    private fun getTypeVariables(p: Predicate): Set<TypeVariable> {
        val types = LinkedHashSet<TypeVariable>()
        p.addTypeVariables(types)
        return types
    }

    fun defined(name: String) =
        classes.containsKey(name)

    private fun getClass(name: String): TypeClass {
        val cl = classes[name]
        if (cl != null)
            return cl
        else
            throw RuntimeException("unknown class '$name'");
    }
}

