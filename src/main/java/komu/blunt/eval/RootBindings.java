package komu.blunt.eval;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static komu.blunt.objects.Symbol.symbol;
import static komu.blunt.types.Predicate.isIn;
import static komu.blunt.types.Qualified.quantify;
import static komu.blunt.types.Type.functionType;
import static komu.blunt.types.Type.tupleType;
import static komu.blunt.types.TypeVariable.tyVar;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import komu.blunt.objects.Symbol;
import komu.blunt.types.Assumptions;
import komu.blunt.types.Kind;
import komu.blunt.types.Qualified;
import komu.blunt.types.Scheme;
import komu.blunt.types.Type;
import komu.blunt.types.TypeVariable;

public final class RootBindings {
    public final StaticEnvironment staticEnvironment = new StaticEnvironment();
    final RootEnvironment runtimeEnvironment = new RootEnvironment();
    private final Map<Symbol, Scheme> types = new HashMap<Symbol, Scheme>();

    public void bind(String name, Type type, Object value) {
        bind(name, type.toScheme(), value);
    }

    public void bind(String name, Scheme type, Object value) {
        if (Arrays.asList("primitiveOpPlus", "primitiveOpMultiply", "primitiveOpMinus", "primitiveOpDivide", "primitiveMod").contains(name)) {
            TypeVariable var = tyVar("a", Kind.STAR);
            Qualified<Type> t = new Qualified<Type>(singletonList(isIn("Num", var)), functionType(tupleType(var, var), var));
            Scheme scheme = quantify(singleton(var), t);
            bind(symbol(name), scheme, value);
        } else if (name.equals("primitiveOpEq")) {
            TypeVariable var = tyVar("a", Kind.STAR);
            Qualified<Type> t = new Qualified<Type>(singletonList(isIn("Eq", var)), functionType(tupleType(var, var), Type.BOOLEAN));
            Scheme scheme = quantify(singleton(var), t);
            bind(symbol(name), scheme, value);
        } else if (Arrays.asList("primitiveOpLt", "primitiveOpLe", "primitiveOpGt", "primitiveOpGe").contains(name)) {
            TypeVariable var = tyVar("a", Kind.STAR);
            Qualified<Type> t = new Qualified<Type>(singletonList(isIn("Ord", var)), functionType(tupleType(var, var), Type.BOOLEAN));
            Scheme scheme = quantify(singleton(var), t);
            bind(symbol(name), scheme, value);

        } else {
            bind(symbol(name), type, value);
        }
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
