package komu.blunt.types;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Collections.singletonMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import komu.blunt.eval.TypeCheckException;
import komu.blunt.objects.Symbol;

public final class Assumptions implements Types<Assumptions> {

    private final Map<Symbol, Scheme> mappings = new HashMap<Symbol,Scheme>();

    public Assumptions() {
    }
    
    public Assumptions(Map<Symbol, Scheme> mappings) {
        this.mappings.putAll(mappings);    
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
        Assumptions as = new Assumptions();
     
        for (Map.Entry<Symbol, Scheme> entry : mappings.entrySet())
            as.mappings.put(entry.getKey(), entry.getValue().apply(substitution));

        return as;
    }

    public static Assumptions singleton(Symbol arg, Scheme scheme) {
        return new Assumptions(singletonMap(arg, scheme));
    }
    
    public Assumptions extend(Symbol arg, Scheme scheme) {
        return singleton(arg, scheme).join(this);
    }

    public Assumptions join(Assumptions as) {
        Assumptions result = new Assumptions(as.mappings);
        result.mappings.putAll(mappings);
        return result;
    }

    public static Assumptions from(List<Symbol> names, List<Scheme> schemes) {
        checkArgument(names.size() == schemes.size(), names.size() + " != " + schemes.size());

        Assumptions as = new Assumptions();
        for (int i = 0; i < names.size(); i++)
            as.mappings.put(names.get(i), schemes.get(i));
        return as;
    }
}
