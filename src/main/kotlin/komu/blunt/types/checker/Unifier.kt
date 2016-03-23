package komu.blunt.types.checker

import komu.blunt.types.Predicate
import komu.blunt.types.Type

object Unifier {

    fun mgu(lhs: Type, rhs: Type): Substitution =
        when {
            lhs is Type.App && rhs is Type.App               -> unifyApplication(lhs, rhs)
            lhs is Type.Var                                  -> varBind(lhs, rhs)
            rhs is Type.Var                                  -> varBind(rhs, lhs)
            lhs is Type.Con && rhs is Type.Con && lhs == rhs -> Substitution.empty
            else -> unificationFailure("types do not unify", lhs, rhs)
        }

    fun match(lhs: Type, rhs: Type): Substitution =
        when {
            lhs is Type.App && rhs is Type.App               -> matchApplication(lhs, rhs)
            lhs is Type.Var && lhs.kind == rhs.kind          -> Substitution.singleton(lhs, rhs)
            lhs is Type.Con && rhs is Type.Con && lhs == rhs -> Substitution.empty
            else -> unificationFailure("types do not match", lhs, rhs)
        }

    fun mguPredicate(left: Predicate, right: Predicate): Substitution =
        if (left.className == right.className)
            mgu(left.predicateType, right.predicateType)
        else
            unificationFailure("classes differ", left, right)

    fun matchPredicate(left: Predicate, right: Predicate): Substitution =
        if (left.className == right.className)
            match(left.predicateType, right.predicateType)
        else
            unificationFailure("classes differ", left, right)

    private fun matchApplication(lhs: Type.App, rhs: Type.App): Substitution {
        val sl = match(lhs.left, rhs.left)
        val sr = match(lhs.right, rhs.right)

        return sl.merge(sr)
    }

    private fun unifyApplication(lhs: Type.App, rhs: Type.App): Substitution {
        val s1 = mgu(lhs.left, rhs.left)
        val s2 = mgu(lhs.right.apply(s1), rhs.right.apply(s1))

        return s2.compose(s1)
    }

    private fun varBind(u: Type.Var, t: Type): Substitution =
        when {
            t == u               -> Substitution.empty
            u in t.typeVars() -> unificationFailure("occurs check fails", u, t)
            u.kind != t.kind     -> unificationFailure("kinds do not match", u, t)
            else                 -> Substitution.singleton(u, t)
        }

    private fun unificationFailure(message: String, u: Any, t: Any) =
        throw UnificationException("$message: $u - $t")
}

