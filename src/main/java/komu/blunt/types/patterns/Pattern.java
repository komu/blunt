package komu.blunt.types.patterns;

import com.google.common.collect.ImmutableList;
import komu.blunt.objects.Symbol;
import komu.blunt.types.ConstructorNames;

import static komu.blunt.objects.Symbol.symbol;

public abstract class Pattern {

    @Override
    public abstract String toString();

    public static Pattern constructor(String name, Pattern... args) {
        //return new ConstructorPattern(name, ImmutableList.copyOf(args));
        return null;
    }
    
    public static Pattern constructor(String name, ImmutableList<Pattern> args) {
        //return new ConstructorPattern(name, args);
        return null;
    }

    public static Pattern variable(String name) {
        //return variable(symbol(name));
        return null;
    }

    public static Pattern variable(Symbol name) {
        //return new VariablePattern(name);
        return null;
    }

    public static Pattern literal(Object value) {
        //return new LiteralPattern(value);
        return null;
    }
    
    public static Pattern wildcard() {
        //return WildcardPattern.INSTANCE;
        return null;
    }

    public static Pattern tuple(ImmutableList<Pattern> args) {
        /*
        if (args.isEmpty())
            return constructor(ConstructorNames.UNIT);
        if (args.size() == 1)
            return args.get(0);
        else
            return constructor(ConstructorNames.tupleName(args.size()), args);
            */
        return null;
    }
}
