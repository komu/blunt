package komu.blunt.eval;

import komu.blunt.analyzer.StaticEnvironment;
import komu.blunt.analyzer.VariableReference;
import komu.blunt.objects.Symbol;
import komu.blunt.types.*;
import komu.blunt.types.checker.Assumptions;

import java.util.Arrays;

import static java.util.Collections.singletonList;
import static komu.blunt.objects.Symbol.symbol;
import static komu.blunt.types.Predicate.isIn;
import static komu.blunt.types.Qualified.quantify;
import static komu.blunt.types.Type.functionType;
import static komu.blunt.types.Type.tupleType;
import static komu.blunt.types.TypeVariable.tyVar;

public final class RootBindings {
    public final StaticEnvironment staticEnvironment = new StaticEnvironment();
    final RootEnvironment runtimeEnvironment = new RootEnvironment();
    private final Assumptions.Builder assumptions = Assumptions.builder();

    public void bind(String name, Scheme type, Object value) {
        if (Arrays.asList("primitiveOpPlus", "primitiveOpMultiply", "primitiveOpMinus", "primitiveOpDivide", "primitiveMod").contains(name)) {
            TypeVariable var = tyVar("a", Kind.STAR);
            Scheme scheme = quantify(var, new Qualified<Type>(singletonList(isIn("Num", var)), functionType(tupleType(var, var), var)));
            bind(symbol(name), scheme, value);
        } else if (name.equals("primitiveOpEq")) {
            TypeVariable var = tyVar("a", Kind.STAR);
            Qualified<Type> t = new Qualified<Type>(singletonList(isIn("Eq", var)), functionType(tupleType(var, var), Type.BOOLEAN));
            Scheme scheme = quantify(var, t);
            bind(symbol(name), scheme, value);
        } else if (Arrays.asList("primitiveOpLt", "primitiveOpLe", "primitiveOpGt", "primitiveOpGe").contains(name)) {
            TypeVariable var = tyVar("a", Kind.STAR);
            Qualified<Type> t = new Qualified<Type>(singletonList(isIn("Ord", var)), functionType(tupleType(var, var), Type.BOOLEAN));
            Scheme scheme = quantify(var, t);
            bind(symbol(name), scheme, value);

        } else {
            bind(symbol(name), type, value);
        }
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
