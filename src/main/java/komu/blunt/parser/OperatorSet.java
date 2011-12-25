package komu.blunt.parser;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static komu.blunt.parser.Operator.*;

public final class OperatorSet {
    
    private final List<List<Operator>> levels = new ArrayList<List<Operator>>();
    
    public OperatorSet() {
        add(2, EQ, LT, LE, GT, GE);
        add(3, PLUS, MINUS);
        add(4, MULTIPLY, DIVIDE);
    }
    
    public void add(int precedence, Operator... operators) {
        checkArgument(precedence >= 0);

        ensureSize(precedence);

        levels.get(precedence).addAll(asList(operators));    
    }

    public Associativity getAssociativity(Operator operator) {
        return operator.toString().equals(":") ? Associativity.RIGHT : Associativity.LEFT;
    }

    public int getMaxLevel() {
        return levels.size()-1;
    }

    public List<Operator> operator(int precedence) {
        return unmodifiableList(levels.get(precedence));
    }

    private void ensureSize(int precedence) {
        while (precedence >= levels.size())
            levels.add(new ArrayList<Operator>());
    }
}
