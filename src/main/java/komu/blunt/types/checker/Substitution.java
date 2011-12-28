package komu.blunt.types.checker;

import com.google.common.collect.ImmutableMap;
import komu.blunt.eval.TypeCheckException;
import komu.blunt.types.Type;
import komu.blunt.types.TypeGen;
import komu.blunt.types.TypeVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Substitution {
    
    private final ImmutableMap<TypeVariable,Type> mapping;

    private Substitution(ImmutableMap<TypeVariable,Type> mapping) {
        this.mapping = checkNotNull(mapping);
    }
    
    public static Substitution empty() {
        return new Substitution(ImmutableMap.<TypeVariable,Type>of());
    }

    public static Substitution singleton(TypeVariable var, Type type) {
        assert var.getKind().equals(type.getKind());

        return new Substitution(ImmutableMap.of(var, type));
    }

    public static Substitution fromTypeVariables(List<TypeVariable> variables) {
        ImmutableMap.Builder<TypeVariable,Type> builder = ImmutableMap.builder();

        int index = 0;
        for (TypeVariable var : variables)
            builder.put(var, new TypeGen(index++));
        
        return new Substitution(builder.build());
    }

    // @@
    public Substitution compose(Substitution s2) {
        ImmutableMap.Builder<TypeVariable,Type> builder = ImmutableMap.builder();

        for (Map.Entry<TypeVariable,Type> entry : s2.mapping.entrySet())
            builder.put(entry.getKey(), entry.getValue().apply(this));

        builder.putAll(mapping);

        return new Substitution(builder.build());
    }
    
    public Substitution merge(Substitution s2) {
        if (agree(s2)) {
            ImmutableMap.Builder<TypeVariable,Type> builder = ImmutableMap.builder();
            builder.putAll(mapping);
            builder.putAll(s2.mapping);
            
            return new Substitution(builder.build());
        } else
            throw new TypeCheckException("merge failed");
    }

    private boolean agree(Substitution s2) {
        for (TypeVariable var : mapping.keySet())
            if (s2.mapping.containsKey(var))
                if (!var.apply(this).equals(var.apply(s2)))
                    return false;

        return true;
    }

    public Substitution apply(Substitution subst) {
        ImmutableMap.Builder<TypeVariable,Type> builder = ImmutableMap.builder();

        for (Map.Entry<TypeVariable,Type> entry : mapping.entrySet())
            builder.put(entry.getKey(), entry.getValue().apply(subst));

        return new Substitution(builder.build());
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
}
