package komu.blunt.eval;

import komu.blunt.objects.Symbol;
import komu.blunt.types.Assumptions;
import komu.blunt.types.Scheme;
import komu.blunt.types.Type;

import java.util.HashMap;
import java.util.Map;

import static komu.blunt.objects.Symbol.symbol;

public final class RootBindings {
    public final StaticEnvironment staticEnvironment = new StaticEnvironment();
    final RootEnvironment runtimeEnvironment = new RootEnvironment();
    private final Map<Symbol, Scheme> types = new HashMap<Symbol, Scheme>();

    public void bind(String name, Type type, Object value) {
        bind(name, type.toScheme(), value);
    }

    public void bind(String name, Scheme type, Object value) {
        bind(symbol(name), type, value);
    }

    public void bind(Symbol name, Scheme type, Object value) {
        VariableReference ref = staticEnvironment.define(name);
        defineVariableType(name, type);
        runtimeEnvironment.define(ref, value);
    }

    public void defineVariableType(Symbol name, Scheme type) {
        types.put(name, type);
    }

    public Assumptions createAssumptions() {
        return new Assumptions(types);
    }
}
