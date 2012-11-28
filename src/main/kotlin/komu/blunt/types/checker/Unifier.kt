package komu.blunt.types.checker

import komu.blunt.types.*

object Unifier {

    fun mgu(lhs: Type, rhs: Type): Substitution =
        when {
            lhs is TypeApplication && rhs is TypeApplication               -> unifyApplication(lhs, rhs)
            lhs is TypeVariable                                            -> varBind(lhs, rhs)
            rhs is TypeVariable                                            -> varBind(rhs, lhs)
            lhs is TypeConstructor && rhs is TypeConstructor && lhs == rhs -> Substitutions.empty()
            else ->
                throw unificationFailure("types do not unify", lhs, rhs)
        }

    fun match(lhs: Type, rhs: Type): Substitution =
        when {
            lhs is TypeApplication && rhs is TypeApplication               -> matchApplication(lhs, rhs)
            lhs is TypeVariable && lhs.kind == rhs.kind                    -> Substitutions.singleton(lhs, rhs)
            lhs is TypeConstructor && rhs is TypeConstructor && lhs == rhs -> Substitutions.empty()
            else ->
                throw unificationFailure("types do not match", lhs, rhs)
        }

    fun mguPredicate(left: Predicate, right: Predicate): Substitution =
        if (left.className == right.className)
            mgu(left.predicateType, right.predicateType)
        else
            throw unificationFailure("classes differ", left, right)

    fun matchPredicate(left: Predicate, right: Predicate): Substitution =
        if (left.className == right.className)
            match(left.predicateType, right.predicateType)
        else
            throw unificationFailure("classes differ", left, right)

    private fun matchApplication(lhs: TypeApplication, rhs: TypeApplication): Substitution {
        val sl = match(lhs.left, rhs.left)
        val sr = match(lhs.right, rhs.right)
        return sl.merge(sr)
    }

    private fun unifyApplication(lhs: TypeApplication, rhs: TypeApplication): Substitution {
        val s1 = mgu(lhs.left, rhs.left)
        val s2 = mgu(lhs.right.apply(s1), rhs.right.apply(s1))

        return s2.compose(s1)
    }

    private fun varBind(u: TypeVariable, t: Type): Substitution =
        when {
            t == u               -> Substitutions.empty()
            u in t.typeVariables -> throw unificationFailure("occurs check fails", u, t)
            u.kind != t.kind     -> throw unificationFailure("kinds do not match", u, t)
            else                 -> Substitutions.singleton(u, t)
        }

    private fun unificationFailure(message: String, u: Any, t: Any) =
        UnificationException("$message: $u - $t")
}

