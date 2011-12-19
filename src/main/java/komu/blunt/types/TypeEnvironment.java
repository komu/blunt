package komu.blunt.types;

import komu.blunt.ast.Expression;
import komu.blunt.eval.TypeCheckException;
import komu.blunt.objects.Symbol;

import java.util.HashMap;
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

        extendSubstitution(mostGeneralUnifier(lhs, rhs));
    }

    private void extendSubstitution(Substitution newSubstitution) {
        setSubstitution(getSubstitution().apply(newSubstitution).union(newSubstitution));
    }

    private Substitution mostGeneralUnifier(Type lhs, Type rhs) {
        if (lhs instanceof TypeVariable)
            return varBind((TypeVariable) lhs, rhs);
        else if (rhs instanceof TypeVariable)
            return varBind((TypeVariable) rhs, lhs);
        else if (lhs.equals(rhs))
            return Substitution.empty();
        else if (lhs instanceof TypeApplication && rhs instanceof TypeApplication)
            return unifyApplication((TypeApplication) lhs, (TypeApplication) rhs);
        else if (lhs instanceof TypeConstructor && rhs instanceof TypeConstructor && lhs.equals(rhs))
            return Substitution.empty();
        else
            throw new TypeCheckException("type unification failed: " + lhs + " - " + rhs);
    }

    private Substitution unifyApplication(TypeApplication lhs, TypeApplication rhs) {
        Substitution s1 = mostGeneralUnifier(lhs.left, rhs.left);
        Substitution s2 = mostGeneralUnifier(lhs.right.apply(s1), rhs.right.apply(s1));

        return s2.join(s1);
    }

    private Substitution varBind(TypeVariable u, Type t) {
        if (t.equals(u)) 
            return Substitution.empty();
        
        if (t.getTypeVariables().contains(u))
            throw new TypeCheckException("type unification failed: " + u + " - " + t);
        
        if (u.getKind().equals(t.getKind()))
            return Substitution.singleton(u, t);

        throw new TypeCheckException("type unification failed: " + u + " - " + t);
    }

    public Substitution getSubstitution() {
        return parent != null ? parent.getSubstitution() : substitution;
    }

    private void setSubstitution(Substitution substitution) {
        if (parent != null)
            parent.setSubstitution(substitution);
        else
            this.substitution = substitution;
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

    public TypeVariable newVar(Kind kind) {
        return TypeVariable.newVar(kind);
    }

    public Type typeCheck(Expression expression) {
        Type type = expression.typeCheck(this);
        return type.apply(getSubstitution());
    }
}
