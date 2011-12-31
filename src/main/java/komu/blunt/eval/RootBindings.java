package komu.blunt.eval;

import komu.blunt.analyzer.StaticEnvironment;
import komu.blunt.analyzer.VariableReference;
import komu.blunt.objects.Symbol;
import komu.blunt.types.DataTypeDefinitions;
import komu.blunt.types.Scheme;
import komu.blunt.types.checker.Assumptions;

import static komu.blunt.objects.Symbol.symbol;

public final class RootBindings {
    public final StaticEnvironment staticEnvironment = new StaticEnvironment();
    final RootEnvironment runtimeEnvironment = new RootEnvironment();
    private final Assumptions.Builder assumptions = Assumptions.builder();
    public final DataTypeDefinitions dataTypes = new DataTypeDefinitions();

    public void bind(String name, Scheme type, Object value) {
        bind(symbol(name), type, value);
    }

    public void bind(Symbol name, Scheme scheme, Object value) {
        VariableReference ref = staticEnvironment.define(name);
        defineVariableType(name, scheme);
        runtimeEnvironment.define(ref, value);
    }

    public void defineVariableType(Symbol name, Scheme scheme) {
        assumptions.add(name, scheme);
    }

    public Assumptions createAssumptions() {
        return assumptions.build();
    }
}
