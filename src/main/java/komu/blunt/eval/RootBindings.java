package komu.blunt.eval;

import komu.blunt.objects.Symbol;
import komu.blunt.types.Type;
import komu.blunt.types.TypeEnvironment;
import komu.blunt.types.TypeScheme;

import java.util.HashMap;
import java.util.Map;

import static komu.blunt.objects.Symbol.symbol;

public final class RootBindings {
    final StaticEnvironment staticEnvironment = new StaticEnvironment();
    final RootEnvironment runtimeEnvironment = new RootEnvironment();
    private final Map<Symbol, TypeScheme> types = new HashMap<Symbol, TypeScheme>();

    public void bind(String name, Type type, Object value) {
        bind(name, new TypeScheme(type), value);
    }

    public void bind(String name, TypeScheme type, Object value) {
        bind(symbol(name), type, value);
    }

    public void bind(Symbol name, TypeScheme type, Object value) {
        VariableReference ref = staticEnvironment.define(name);
        defineVariableType(name, type);
        runtimeEnvironment.define(ref, value);
    }

    public TypeEnvironment createTypeEnvironment() {
        TypeEnvironment typeEnvironment = new TypeEnvironment();
    
        for (Map.Entry<Symbol, TypeScheme> entry : types.entrySet())
            typeEnvironment.bind(entry.getKey(), entry.getValue());

        return typeEnvironment;
    }
    
    public void defineVariableType(Symbol name, TypeScheme type) {
        types.put(name, type);
    }
}
