package komu.blunt.eval;

import komu.blunt.objects.Symbol;
import komu.blunt.types.Type;
import komu.blunt.types.TypeEnvironment;
import komu.blunt.types.TypeScheme;

import static komu.blunt.objects.Symbol.symbol;

final class RootBindings {
    final StaticEnvironment staticEnvironment = new StaticEnvironment();
    final TypeEnvironment typeEnvironment = new TypeEnvironment();
    final RootEnvironment runtimeEnvironment = new RootEnvironment();

    public void bind(String name, Type type, Object value) {
        bind(name, new TypeScheme(type), value);
    }

    public void bind(String name, TypeScheme type, Object value) {
        bind(symbol(name), type, value);
    }

    public void bind(Symbol name, TypeScheme type, Object value) {
        VariableReference ref = staticEnvironment.define(name);
        typeEnvironment.bind(name, type);
        runtimeEnvironment.define(ref, value);
    }
}
