package komu.blunt.types.checker;

import komu.blunt.types.TypeVariable;
import komu.blunt.types.Types;

import java.util.*;

public final class TypeUtils {

    public static void addTypeVariables(Set<TypeVariable> variables, Iterable<? extends Types<?>> ts) {
        for (Types<?> t : ts)
            t.addTypeVariables(variables);
    }
    
    public static Set<TypeVariable> getTypeVariables(Types<?> t) {
        LinkedHashSet<TypeVariable> types = new LinkedHashSet<TypeVariable>();
        t.addTypeVariables(types);
        return types;
    }

    public static <T extends Types<T>> List<T> applySubstitution(Substitution substitution, Collection<? extends T> ts) {
        List<T> result = new ArrayList<T>(ts.size());
        
        for (T t : ts)
            result.add(t.apply(substitution));

        return result;
    }
}
