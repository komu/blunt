package komu.blunt.parser;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Math.max;

public final class OperatorSet {
    
    private final Map<String,Operator> ops = new HashMap<>();
    private int maxPrecedence = 0;
    private static final int DEFAULT_PRECEDENCE = 0;
    
    public OperatorSet() {
        add(1, Associativity.RIGHT, ":");
        add(2, "==", "<", "<=", ">", ">=");
        add(3, "+", "-");
        add(4, "*", "/", "%");
    }

    public void add(int precedence, String... names) {
        add(precedence, Associativity.LEFT, names);
    }
    
    public void add(int precedence, Associativity associativity, String... names) {
        checkArgument(precedence >= 0);

        maxPrecedence = max(maxPrecedence, precedence);
        for (String name : names)
            ops.put(name, new Operator(name, associativity, precedence));
    }

    public Operator operator(String name) {
        Operator op = ops.get(name);
        if (op != null)
            return op;
        else
            return new Operator(name, Associativity.LEFT, DEFAULT_PRECEDENCE);
    }

    public int getMaxLevel() {
        return maxPrecedence;
    }
}
