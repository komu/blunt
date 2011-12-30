package komu.blunt.ast;

import com.google.common.collect.ImmutableList;
import komu.blunt.objects.Symbol;
import komu.blunt.types.ConstructorDefinition;
import komu.blunt.types.patterns.Pattern;

import java.util.List;

import static java.util.Arrays.asList;
import static komu.blunt.objects.Symbol.symbol;

/**
 * Convenience methods for constructing syntax objects.
 */
public final class AST {

    private AST() { }

    public static ASTDataDefinition data(String name, ImmutableList<ConstructorDefinition> constructors) {
        return new ASTDataDefinition(name, constructors);
    }

    public static ASTDataDefinition data(String name, ConstructorDefinition... constructors) {
        return data(name, ImmutableList.copyOf(constructors));
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
        
        return exp;
    }

    public static ASTExpression constructor(String name, ASTExpression... args) {
        return apply(new ASTConstructor(name), args);
    }
    
    public static ASTExpression lambda(Symbol argument, ASTExpression body) {
        return new ASTLambda(asList(argument), body);
    }

    public static ASTExpression lambda(List<Symbol> arguments, ASTExpression body) {
        return new ASTLambda(arguments, body);
    }

    public static ASTExpression ifExp(ASTExpression test, ASTExpression cons, ASTExpression alt) {
        return new ASTIf(test, cons, alt);
    }

    public static ASTExpression caseExp(ASTExpression exp, ImmutableList<ASTAlternative> alts) {
        return new ASTCase(exp, alts);
    }

    public static ASTAlternative alternative(Pattern pattern, ASTExpression exp) {
        return new ASTAlternative(pattern, exp);
    }

    public static ASTExpression letRec(Symbol name, ASTExpression value, ASTExpression body) {
        return new ASTLetRec(asList(new ImplicitBinding(name, value)), body);
    }

    public static ASTExpression letRec(List<ImplicitBinding> bindings, ASTExpression body) {
        return new ASTLetRec(bindings, body);
    }

    public static ASTExpression let(Symbol name, ASTExpression value, ASTExpression body) {
        return new ASTLet(asList(new ImplicitBinding(name, value)), body);
    }

    public static ASTExpression let(List<ImplicitBinding> bindings, ASTExpression body) {
        return new ASTLet(bindings, body);
    }
    
    public static ASTSequence sequence(List<ASTExpression> exps) {
        return new ASTSequence(exps);
    }

    public static ASTSequence sequence(ASTExpression... exps) {
        return new ASTSequence(asList(exps));
    }

    public static ASTExpression tuple(List<ASTExpression> exps) {
        return new ASTTuple(exps);
    }

    public static ASTExpression set(Symbol name, ASTExpression exp) {
        return new ASTSet(name, exp);
    }

    public static ASTValueDefinition define(Symbol name, ASTExpression value) {
        return new ASTValueDefinition(name, value);
    }
}
