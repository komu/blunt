package komu.blunt.ast;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import komu.blunt.objects.Symbol;
import komu.blunt.types.ConstructorDefinition;
import komu.blunt.types.Type;
import komu.blunt.types.patterns.Pattern;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static komu.blunt.objects.Symbol.symbol;
import static komu.blunt.types.DataTypeDefinitions.*;

/**
 * Convenience methods for constructing syntax objects.
 */
public final class AST {

    private AST() { }

    public static ASTDataDefinition data(String name, Type type, ImmutableList<ConstructorDefinition> constructors, ImmutableList<String> derivedClasses) {
        return new ASTDataDefinition(name, type, constructors, derivedClasses);
    }

    public static ASTExpression constant(Object value) {
        return new ASTConstant(value);
    }

    public static ASTExpression variable(String name) {
        return new ASTVariable(symbol(name));
    }

    public static ASTExpression variable(Symbol name) {
        return new ASTVariable(name);
    }

    public static ASTExpression apply(ASTExpression func, ASTExpression... args) {
        ASTExpression exp = func;
        
        for (ASTExpression arg : args)
            exp = new ASTApplication(exp, arg);
        
        return exp.simplify();
    }

    public static ASTExpression constructor(String name, ASTExpression... args) {
        return apply(new ASTConstructor(name), args);
    }
    
    public static ASTExpression lambda(Symbol argument, ASTExpression body) {
        return new ASTLambda(argument, body);
    }

    public static ASTExpression lambda(List<Symbol> arguments, ASTExpression body) {
        if (arguments.isEmpty()) throw new IllegalArgumentException("no arguments for lambda");
        
        Symbol head = arguments.get(0);
        List<Symbol> tail = arguments.subList(1, arguments.size());
        if (arguments.size() == 1)
            return lambda(head, body);
        else
            return lambda(head, lambda(tail, body));
    }

    public static ASTExpression ifExp(ASTExpression test, ASTExpression cons, ASTExpression alt) {
        return caseExp(test, alternative(Pattern.constructor(TRUE), cons),
                             alternative(Pattern.constructor(FALSE), alt));
    }

    public static ASTExpression caseExp(ASTExpression exp, ImmutableList<ASTAlternative> alts) {
        return new ASTCase(exp, alts);
    }

    public static ASTExpression caseExp(ASTExpression exp, ASTAlternative... alts) {
        return new ASTCase(exp, ImmutableList.copyOf(alts));
    }
    
    public static ASTAlternative alternative(Pattern pattern, ASTExpression exp) {
        return new ASTAlternative(pattern, exp);
    }

    public static ASTExpression letRec(Symbol name, ASTExpression value, ASTExpression body) {
        return new ASTLetRec(ImmutableList.of(new ImplicitBinding(name, value)), body);
    }

    public static ASTExpression let(boolean recursive, ImplicitBinding binding, ASTExpression body) {
        ImmutableList<ImplicitBinding> bindings = ImmutableList.of(binding);
        return recursive ? new ASTLetRec(bindings, body) : new ASTLet(bindings, body);
    }

    public static ASTSequence sequence(ASTExpression... exps) {
        return new ASTSequence(ImmutableList.copyOf(exps));
    }

    public static ASTExpression tuple(List<ASTExpression> exps) {
        if (exps.isEmpty())
            return AST.constructor(UNIT);
        if (exps.size() == 1)
            return exps.get(0);

        ASTExpression call =  AST.constructor(tupleName(exps.size()));

        for (ASTExpression exp : exps)
            call = new ASTApplication(call, exp);

        return call;
    }

    public static ASTExpression set(Symbol name, ASTExpression exp) {
        return new ASTSet(name, exp);
    }

    public static ASTValueDefinition define(Symbol name, ASTExpression value) {
        return new ASTValueDefinition(name, value);
    }
    
    public static ListBuilder listBuilder() {
        return new ListBuilder();
    }

    public static final class ListBuilder {
        private final List<ASTExpression> exps = newArrayList();
        
        public void add(ASTExpression exp) {
            exps.add(exp);
        }
        
        public ASTExpression build() {
            ASTExpression list = constructor(NIL);

            for (ASTExpression exp : Lists.reverse(exps))
                list = constructor(CONS, exp, list);

            return list;
        }
    }

    public static SequenceBuilder sequenceBuilder() {
        return new SequenceBuilder();
    }

    public static final class SequenceBuilder {
        private final ImmutableList.Builder<ASTExpression> exps = ImmutableList.builder();

        public void add(ASTExpression exp) {
            exps.add(exp);
        }

        public ASTSequence build() {
            return new ASTSequence(exps.build());
        }
    }
}
