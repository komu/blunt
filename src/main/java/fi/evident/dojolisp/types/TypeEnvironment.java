package fi.evident.dojolisp.types;

import fi.evident.dojolisp.eval.TypeCheckException;
import fi.evident.dojolisp.objects.Symbol;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TypeEnvironment {
    
    private final Map<Symbol, TypeScheme> bindings = new HashMap<Symbol, TypeScheme>();
    private final TypeEnvironment parent;
    private Substitution substitution; // TODO: move to some type-checker

    public TypeEnvironment() {
        this(null);

        substitution = Substitution.empty();
    }

    public TypeEnvironment(TypeEnvironment parent) {
        this.parent = parent;
    }

    public void assign(Type left, Type right) {
        unify(left, right);
    }    
    
    public void unify(Type left, Type right) {
        Substitution subst = getSubstitution();

        Type lhs = left.apply(subst);
        Type rhs = right.apply(subst);
        
        Substitution s = mgu(lhs, rhs);
        if (s != null)
            extendSubstitution(s);
        else
            throw new TypeCheckException("type unification failed: " + left + " - " + right);
    }

    private void extendSubstitution(Substitution newSubstitution) {
        setSubstitution(getSubstitution().apply(newSubstitution).union(newSubstitution));
    }

    private Substitution mgu(Type lhs, Type rhs) {
        if (lhs instanceof TypeVariable)
            return varBind((TypeVariable) lhs, rhs);
        else if (rhs instanceof TypeVariable)
            return varBind((TypeVariable) rhs, lhs);

        if (lhs.equals(rhs))
            return Substitution.empty();

        /*
> mgu :: Type -> Type -> Maybe Subst
> mgu (TAp l r)  (TAp l' r')          = do s1 <- mgu l l'
>                                          s2 <- mgu (apply s1 r) (apply s1 r')
>                                          Just (s2 @@ s1)
> mgu (TCon c1)  (TCon c2) | c1 == c2 = Just nullSubst
> mgu _         _                     = Nothing

         */
        // TODO
        return null;
    }
    
    private Substitution varBind(TypeVariable u, Type t) {
        if (t.equals(u)) 
            return Substitution.empty();
        
        if (t.getTypeVariables().contains(u))
            return null;
        
        if (u.getKind().equals(t.getKind()))
            return Substitution.singleton(u, t);

        return null;
    }

    private Substitution getSubstitution() {
        return parent != null ? parent.getSubstitution() : substitution;
    }

    private void setSubstitution(Substitution substitution) {
        if (parent != null)
            parent.setSubstitution(substitution);
        else
            this.substitution = substitution;
    }

    public Type call(Type func, List<Type> argTypes) {
        return func.asFunctionType().typeCheckCall(this, argTypes);
    }

    public void bind(Symbol symbol, TypeScheme typeScheme) {
        bindings.put(symbol, typeScheme);
    }

    public TypeScheme lookup(Symbol name) {
        TypeScheme scheme = bindings.get(name);

        if (scheme != null)
            return scheme;
        else if (parent != null)
            return parent.lookup(name);
        else
            throw new IllegalArgumentException("unknown binding: " + name);
    }
}
