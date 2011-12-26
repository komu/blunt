package komu.blunt.types;

import java.util.*;

import static com.google.common.collect.Sets.intersection;
import static java.util.Collections.singletonMap;

public final class Substitution {
    
    private final Map<TypeVariable,Type> mapping = new HashMap<TypeVariable, Type>();

    private Substitution() {
    }
    
    private Substitution(Map<TypeVariable, Type> mapping) {
        this.mapping.putAll(mapping);
    }
    
    public Substitution(List<TypeVariable> variables) {
        int index = 0;
        for (TypeVariable var : variables)
            mapping.put(var, new TypeGen(index++));
    }

    private Substitution(Map<TypeVariable, Type> m1, Map<TypeVariable, Type> m2) {
        mapping.putAll(m1);
        mapping.putAll(m2);
    }

    // @@
    public Substitution compose(Substitution s2) {
        Substitution result = new Substitution();
        
        for (Map.Entry<TypeVariable,Type> entry : s2.mapping.entrySet())
            result.mapping.put(entry.getKey(), entry.getValue().apply(this));

        result.mapping.putAll(mapping);

        return result;
    }
    
    public Substitution merge(Substitution s2) {
        if (agree(s2))
            return new Substitution(mapping, s2.mapping);
        else
            throw new RuntimeException("merge fails");
    }

    private boolean agree(Substitution s2) {
        for (TypeVariable var : intersection(variables(), s2.variables()))
            if (!var.apply(this).equals(var.apply(s2)))
                return false;

        return true;
    }
    
    private Set<TypeVariable> variables() {
        return mapping.keySet();
    }

    public Substitution apply(Substitution subst) {
        Substitution result = new Substitution();

        for (Map.Entry<TypeVariable,Type> entry : mapping.entrySet())
            result.mapping.put(entry.getKey(), entry.getValue().apply(subst));

        return result;
    }

    public Type lookup(TypeVariable variable) {
        return mapping.get(variable);
    }
    
    public List<Type> apply(List<Type> types) {
        List<Type> result = new ArrayList<Type>(types.size());
        
        for (Type type : types)
            result.add(type.apply(this));
        
        return result;
    }

    public Substitution union(Substitution subst) {
        return new Substitution(mapping, subst.mapping);
    }

    public static Substitution empty() {
        return new Substitution();
    }

    public static Substitution singleton(TypeVariable var, Type type) {
        assert var.getKind().equals(type.getKind());

        return new Substitution(singletonMap(var, type));
    }
}
