package komu.blunt.types.checker;

import komu.blunt.eval.TypeCheckException;
import komu.blunt.objects.Symbol;
import komu.blunt.types.Scheme;
import komu.blunt.types.TypeVariable;
import komu.blunt.types.Types;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

public final class Assumptions implements Types<Assumptions> {

    private final Map<Symbol, Scheme> mappings;
    
    private Assumptions() {
        this.mappings = emptyMap();
    }

    private Assumptions(Builder builder) {
        this.mappings = unmodifiableMap(builder.mappings);
    }

    public Scheme find(Symbol name) {
        Scheme scheme = mappings.get(name);
        if (scheme != null)
            return scheme;
        else
            throw new TypeCheckException("unbound identifier: " + name);
    }

    @Override
    public String toString() {
        return mappings.toString();
    }

    @Override
    public void addTypeVariables(Set<TypeVariable> variables) {
        for (Scheme scheme : mappings.values())
            scheme.addTypeVariables(variables);
    }

    @Override
    public Assumptions apply(Substitution substitution) {
        Builder builder = builder();
     
        for (Map.Entry<Symbol, Scheme> entry : mappings.entrySet())
            builder.add(entry.getKey(), entry.getValue().apply(substitution));

        return builder.build();
    }

    public static Assumptions singleton(Symbol arg, Scheme scheme) {
        return builder().add(arg, scheme).build();
    }

    public Assumptions join(Assumptions as) {
        return builder().addAll(as).addAll(this).build();
    }

    public static Assumptions from(List<Symbol> names, List<Scheme> schemes) {
        checkArgument(names.size() == schemes.size(), names.size() + " != " + schemes.size());

        Builder builder = new Builder();
        for (int i = 0; i < names.size(); i++)
            builder.add(names.get(i), schemes.get(i));
        
        return builder.build();
    }
    
    public static Builder builder() {
        return new Builder();
    }

    public static Assumptions empty() {
        return new Assumptions();
    }

    public static final class Builder {
        private Map<Symbol, Scheme> mappings = new HashMap<>();
        private boolean built = false;

        public Builder add(Symbol name, Scheme scheme) {
            ensurePrivateCopy();
            mappings.put(checkNotNull(name), checkNotNull(scheme));
            return this;
        }

        public Builder addAll(Assumptions as) {
            ensurePrivateCopy();
            mappings.putAll(as.mappings);
            return this;
        }
        
        public Assumptions build() {
            built = true;
            return new Assumptions(this);
        }

        public Assumptions build(Assumptions as) {
            return build().join(as);
        }
        
        private void ensurePrivateCopy() {
            if (built) {
                mappings = new HashMap<>(mappings);
                built = false;
            }
        }
    }
}
