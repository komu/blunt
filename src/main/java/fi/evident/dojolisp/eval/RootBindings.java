package fi.evident.dojolisp.eval;

import fi.evident.dojolisp.objects.Symbol;
import fi.evident.dojolisp.types.Type;
import fi.evident.dojolisp.types.TypeEnvironment;
import fi.evident.dojolisp.types.TypeScheme;

import static fi.evident.dojolisp.objects.Symbol.symbol;

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
        VariableReference ref = staticEnvironment.define(name, type);
        typeEnvironment.bind(name, type);
        runtimeEnvironment.define(ref, value);
    }
}
