package komu.blunt.analyzer;

import com.google.common.collect.ImmutableList;
import komu.blunt.ast.*;
import komu.blunt.objects.Symbol;
import komu.blunt.types.patterns.*;

/**
 * Walks the AST to rename all local variables so that they become unique. This makes
 * further optimizations simpler.
 */
public final class IdentifierRenamer implements ASTVisitor<IdentifierMapping, ASTExpression>, PatternVisitor<IdentifierMapping, Pattern> {

    private int sequence = 1;

    public static ASTExpression rename(ASTExpression exp) {
        return new IdentifierRenamer().renameIdentifiers(exp, new IdentifierMapping());
    }

    public ASTExpression renameIdentifiers(ASTExpression exp, IdentifierMapping ctx) {
        return exp.accept(this, ctx);
    }

    private Pattern renameIdentifiers(Pattern pattern, IdentifierMapping ctx) {
        return pattern.accept(this, ctx);
    }
    
    private Symbol freshVariable() {
        return Symbol.symbol("$var" +  sequence++);
    }    

    @Override
    public ASTExpression visit(ASTApplication application, IdentifierMapping ctx) {
        return AST.apply(renameIdentifiers(application.func, ctx), renameIdentifiers(application.arg, ctx));
    }

    @Override
    public ASTExpression visit(ASTConstant constant, IdentifierMapping ctx) {
        return constant;
    }

    @Override
    public ASTExpression visit(ASTLambda lambda, IdentifierMapping ctx) {
        IdentifierMapping newCtx = ctx.extend();
        
        Symbol var = freshVariable();
        newCtx.put(lambda.argument, var);

        return AST.lambda(var, renameIdentifiers(lambda.body, newCtx));
    }

    @Override
    public ASTExpression visit(ASTLet let, IdentifierMapping ctx) {
        if (let.bindings.size() != 1) throw new UnsupportedOperationException();
        
        IdentifierMapping newCtx = ctx.extend();

        Symbol var = freshVariable();
        newCtx.put(let.bindings.get(0).name, var);
        
        ImplicitBinding binding = new ImplicitBinding(var, renameIdentifiers(let.bindings.get(0).expr, ctx));
        
        return AST.let(false, binding, renameIdentifiers(let.body, newCtx));
    }

    @Override
    public ASTExpression visit(ASTLetRec let, IdentifierMapping ctx) {
        if (let.bindings.size() != 1) throw new UnsupportedOperationException();

        IdentifierMapping newCtx = ctx.extend();

        Symbol var = freshVariable();
        newCtx.put(let.bindings.get(0).name, var);

        ImplicitBinding binding = new ImplicitBinding(var, renameIdentifiers(let.bindings.get(0).expr, newCtx));

        return AST.let(true, binding, renameIdentifiers(let.body, newCtx));
    }

    @Override
    public ASTExpression visit(ASTSequence sequence, IdentifierMapping ctx) {
        AST.SequenceBuilder result = AST.sequenceBuilder();
        
        for (ASTExpression exp : sequence.exps)
            result.add(renameIdentifiers(exp, ctx));
        
        return result.build();
    }

    @Override
    public ASTExpression visit(ASTSet set, IdentifierMapping ctx) {
        return AST.set(ctx.get(set.var), renameIdentifiers(set.exp, ctx));
    }

    @Override
    public ASTExpression visit(ASTVariable variable, IdentifierMapping ctx) {
        return AST.variable(ctx.get(variable.var));
    }

    @Override
    public ASTExpression visit(ASTConstructor constructor, IdentifierMapping ctx) {
        return constructor;
    }

    @Override
    public ASTExpression visit(ASTCase astCase, IdentifierMapping ctx) {
        ImmutableList.Builder<ASTAlternative> alts = ImmutableList.builder();

        for (ASTAlternative alt : astCase.alternatives) {
            IdentifierMapping newCtx = ctx.extend();
            Pattern pattern = renameIdentifiers(alt.pattern, newCtx);
            alts.add(AST.alternative(pattern, renameIdentifiers(alt.value, newCtx)));
        }

        return AST.caseExp(renameIdentifiers(astCase.exp, ctx), alts.build());
    }

    @Override
    public Pattern visit(ConstructorPattern pattern, IdentifierMapping ctx) {
        ImmutableList.Builder<Pattern> args = ImmutableList.builder();
        
        for (Pattern arg : pattern.args)
            args.add(renameIdentifiers(arg, ctx));
        
        return Pattern.constructor(pattern.name, args.build());
    }

    @Override
    public Pattern visit(LiteralPattern pattern, IdentifierMapping ctx) {
        return pattern;
    }

    @Override
    public Pattern visit(VariablePattern pattern, IdentifierMapping ctx) {
        Symbol var = freshVariable();
        ctx.put(pattern.var, var);
        return Pattern.variable(var);
    }

    @Override
    public Pattern visit(WildcardPattern pattern, IdentifierMapping ctx) {
        return pattern;
    }
}
