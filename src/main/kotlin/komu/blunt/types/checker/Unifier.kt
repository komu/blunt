package komu.blunt.types.checker

import komu.blunt.types.*

object Unifier {

    fun mgu(lhs: Type, rhs: Type): Substitution {
        if (lhs is TypeApplication && rhs is TypeApplication)
            return unifyApplication(lhs, rhs)

        if (lhs is TypeVariable)
            return varBind(lhs, rhs)

        if (rhs is TypeVariable)
            return varBind(rhs, lhs)

        if (lhs is TypeConstructor && rhs is TypeConstructor && lhs == rhs)
            return Substitutions.empty()

        throw unificationFailure("types do not unify", lhs, rhs)
    }

    fun match(lhs: Type, rhs: Type): Substitution  {
        if (lhs is TypeApplication && rhs is TypeApplication)
            return matchApplication(lhs, rhs)

        if (lhs is TypeVariable && lhs.kind == rhs.kind)
            return Substitutions.singleton(lhs, rhs)

        if (lhs is TypeConstructor && rhs is TypeConstructor && lhs == rhs)
            return Substitutions.empty()

        throw unificationFailure("types do not match", lhs, rhs)
    }

    fun mguPredicate(left: Predicate, right: Predicate): Substitution  {
        if (left.className == right.className)
            return mgu(left.`type`, right.`type`)

        throw unificationFailure("classes differ", left, right)
    }

    fun matchPredicate(left: Predicate, right: Predicate): Substitution {
        if (left.className == right.className)
            return match(left.`type`, right.`type`)

        throw unificationFailure("classes differ", left, right)
    }

    private fun matchApplication(lhs: TypeApplication, rhs: TypeApplication): Substitution {
        val sl = match(lhs.left, rhs.left)
        val sr = match(lhs.right, rhs.right)
        return sl.merge(sr)
    }

    private fun unifyApplication(lhs: TypeApplication, rhs: TypeApplication): Substitution {
        val s1 = mgu(lhs.left, rhs.left)
        val s2 = mgu(lhs.right.apply(s1).sure(), rhs.right.apply(s1).sure())

        return s2.compose(s1)
    }

    private fun varBind(u: TypeVariable, t: Type): Substitution {
        if (t == u)
            return Substitutions.empty()

        if (t.containsVariable(u))
            throw unificationFailure("occurs check fails", u, t)

        if (u.kind != t.kind)
            throw unificationFailure("kinds do not match", u, t)

        return Substitutions.singleton(u, t)
    }

    private fun unificationFailure(message: String, u: Any, t: Any) =
        UnificationException("$message: $u - $t")
}

