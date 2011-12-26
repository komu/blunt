package komu.blunt.types;

final class Unifier {

    public static Substitution mgu(Type lhs, Type rhs) throws UnificationException {
        if (lhs instanceof TypeApplication && rhs instanceof TypeApplication)
            return unifyApplication((TypeApplication) lhs, (TypeApplication) rhs);

        if (lhs instanceof TypeVariable)
            return varBind((TypeVariable) lhs, rhs);

        if (rhs instanceof TypeVariable)
            return varBind((TypeVariable) rhs, lhs);

        if (lhs instanceof TypeConstructor && rhs instanceof TypeConstructor && lhs.equals(rhs))
            return Substitution.empty();

        throw unificationFailure("types do not unify", lhs, rhs);
    }

    public static Substitution match(Type lhs, Type rhs) throws UnificationException {
        if (lhs instanceof TypeApplication && rhs instanceof TypeApplication)
            return matchApplication((TypeApplication) lhs, (TypeApplication) rhs);

        if (lhs instanceof TypeVariable && lhs.getKind().equals(rhs.getKind()))
            return Substitution.singleton((TypeVariable) lhs, rhs);

        if (lhs instanceof TypeConstructor && rhs instanceof TypeConstructor && lhs.equals(rhs))
            return Substitution.empty();

        throw unificationFailure("types do not match", lhs, rhs);
    }

    public static Substitution mguPredicate(Predicate left, Predicate right) throws UnificationException {
        if (left.className.equals(right.className))
            return mgu(left.type, right.type);

        throw unificationFailure("classes differ", left, right);
    }

    public static Substitution matchPredicate(Predicate left, Predicate right) throws UnificationException {
        if (left.className.equals(right.className))
            return match(left.type, right.type);

        throw unificationFailure("classes differ", left, right);
    }

    private static Substitution matchApplication(TypeApplication lhs, TypeApplication rhs) throws UnificationException {
        Substitution sl = match(lhs.left, rhs.left);
        Substitution sr = match(lhs.right, rhs.right);
        return sl.merge(sr);
    }

    private static Substitution unifyApplication(TypeApplication lhs, TypeApplication rhs) throws UnificationException {
        Substitution s1 = mgu(lhs.left, rhs.left);
        Substitution s2 = mgu(lhs.right.apply(s1), rhs.right.apply(s1));

        return s2.compose(s1);
    }

    private static Substitution varBind(TypeVariable u, Type t) throws UnificationException {
        if (t.equals(u))
            return Substitution.empty();

        if (t.containsVariable(u))
            throw unificationFailure("occurs check fails", u, t);

        if (!u.getKind().equals(t.getKind()))
            throw unificationFailure("kinds do not match", u, t);

        return Substitution.singleton(u, t);
    }

    private static UnificationException unificationFailure(String message, Object u, Object t) {
        return new UnificationException(message + ": " + u + " - " + t);
    }
}
