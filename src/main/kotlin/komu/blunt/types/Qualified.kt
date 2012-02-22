package komu.blunt.types

import komu.blunt.types.checker.TypeUtils
import java.util.Collection
import java.util.ArrayList
import komu.blunt.types.checker.Substitution
import java.util.List
import java.util.LinkedHashSet

fun quantifyAll(qt: Qualified<Type?>): Scheme {
    val types = LinkedHashSet<TypeVariable?>()
    qt.addTypeVariables(types)
    return quantify(types, qt)
}

fun quantify(vs: Collection<TypeVariable?>, qt: Qualified<Type?>): Scheme {
    val kinds = ArrayList<Kind?>()
    val vars = ArrayList<TypeVariable?>()

    val types = LinkedHashSet<TypeVariable?>()
    qt.addTypeVariables(types)

    for (val v in types)
        if (vs.contains(v)) {
            vars.add(v)
            kinds.add(v?.getKind())
        }

    return Scheme(kinds, qt.apply(Substitution.fromTypeVariables(vars)))
}

fun instantiate(ts: List<TypeVariable?>, t: Qualified<Type?>): Qualified<Type?> {
    val ps = ArrayList<Predicate?>(t.predicates.sure().size())
    for (val p in t.predicates)
        ps.add(p?.instantiate(ts))

    return Qualified(ps, t.value.sure().instantiate(ts))
}
