package komu.blunt.types.patterns;

import com.google.common.collect.ImmutableList;
import komu.blunt.objects.Symbol;

import static komu.blunt.objects.Symbol.symbol;
import static komu.blunt.types.DataTypeDefinitions.UNIT;
import static komu.blunt.types.DataTypeDefinitions.tupleName;

public abstract class Pattern {

    @Override
    public abstract String toString();

    public static Pattern constructor(String name, Pattern... args) {
        return new ConstructorPattern(name, ImmutableList.copyOf(args));
    }
    
    public static Pattern constructor(String name, ImmutableList<Pattern> args) {
        return new ConstructorPattern(name, args);
    }

    public static Pattern variable(String name) {
        return variable(symbol(name));
    }

    public static Pattern variable(Symbol name) {
        return new VariablePattern(name);
    }

    public static Pattern literal(Object value) {
        return new LiteralPattern(value);
    }
    
    public static Pattern wildcard() {
        return WildcardPattern.INSTANCE;
    }

    public static Pattern tuple(ImmutableList<Pattern> args) {
        if (args.isEmpty())
            return constructor(UNIT);
        if (args.size() == 1)
            return args.get(0);
        else
            return constructor(tupleName(args.size()), args);
    }
}
