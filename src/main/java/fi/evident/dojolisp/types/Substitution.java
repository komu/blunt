package fi.evident.dojolisp.types;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Substitution {
    
    private final Map<TypeVariable,Type> mapping = new HashMap<TypeVariable, Type>();

    private Substitution() {
    }

    public Substitution(List<TypeVariable> variables) {
        int index = 0;
        for (TypeVariable var : variables)
            mapping.put(var, new TypeGen(index++));
    }
    
    private Substitution(Substitution parent) {
        mapping.putAll(parent.mapping);
    }

    public Type lookup(TypeVariable variable) {
        return mapping.get(variable);
    }

    public Substitution apply(Substitution subst) {
        Substitution result = new Substitution(this);
        
        for (Map.Entry<TypeVariable,Type> entry : result.mapping.entrySet())
            entry.setValue(entry.getValue().apply(subst));

        return result;
    }

    public Substitution union(Substitution subst) {
        Substitution result = new Substitution(this);
        
        for (Map.Entry<TypeVariable,Type> entry : subst.mapping.entrySet())
            result.mapping.put(entry.getKey(), entry.getValue());

        return result;
    }

    public static Substitution empty() {
        return new Substitution();
    }

    public static Substitution singleton(TypeVariable var, Type type) {
        Substitution subst = new Substitution();
        subst.mapping.put(var, type);
        return subst;
    }
}
