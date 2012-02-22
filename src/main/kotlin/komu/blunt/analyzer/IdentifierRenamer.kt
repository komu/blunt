package komu.blunt.analyzer

import com.google.common.collect.ImmutableList
import komu.blunt.ast.*
import komu.blunt.objects.Symbol
import komu.blunt.types.patterns.*
import java.util.ArrayList

/**
 * Walks the AST to rename all local variables so that they become unique. This makes
 * further optimizations simpler.
 */
class IdentifierRenamer {

    private var sequence = 1

    class object {
        fun rename(exp: ASTExpression): ASTExpression =
            IdentifierRenamer().renameIdentifiers(exp, IdentifierMapping())
    }

    fun renameIdentifiers(exp: ASTExpression, ctx: IdentifierMapping) =
        when (exp) {
            is ASTConstant    -> exp
            is ASTConstructor -> exp
            is ASTApplication -> visit(exp, ctx)
            is ASTLambda      -> visit(exp, ctx)
            is ASTLet         -> visit(exp, ctx)
            is ASTLetRec      -> visit(exp, ctx)
            is ASTSequence    -> visit(exp, ctx)
            is ASTSet         -> visit(exp, ctx)
            is ASTVariable    -> visit(exp, ctx)
            is ASTCase        -> visit(exp, ctx)
            else              -> throw Exception("unknown exp $exp")
        }

    fun renameIdentifiers(pattern: Pattern, ctx: IdentifierMapping) =
        when (pattern) {
            is WildcardPattern    -> pattern
            is LiteralPattern     -> pattern
            is VariablePattern    -> renamePattern(pattern, ctx)
            is ConstructorPattern -> renamePattern(pattern, ctx)
            else                  -> throw Exception("invalid pattern $pattern")
        }

    private fun renamePattern(pattern: ConstructorPattern, ctx: IdentifierMapping): Pattern {
        val args = ArrayList<Pattern>()

        for (val arg in pattern.args)
            args.add(renameIdentifiers(arg.sure(), ctx));

        return Pattern.constructor(pattern.name, ImmutableList.copyOf(args).sure())
    }

    private fun renamePattern(pattern: VariablePattern, ctx: IdentifierMapping): Pattern {
        val v = freshVariable()
        ctx.put(pattern.variable, v)
        return Pattern.variable(v)
    }

    private fun freshVariable() =
        Symbol("\$var${sequence++}")

    private fun visit(sequence: ASTSequence, ctx: IdentifierMapping): ASTExpression {
        val result = AST.sequenceBuilder().sure()

        for (val exp in sequence.exps)
            result.add(renameIdentifiers(exp.sure(), ctx))

        return result.build().sure()
    }

    private fun visit(application: ASTApplication, ctx: IdentifierMapping): ASTExpression =
        AST.apply(renameIdentifiers(application.func, ctx), renameIdentifiers(application.arg, ctx)).sure()

    private fun visit(set: ASTSet, ctx: IdentifierMapping): ASTExpression =
        AST.set(ctx.get(set.variable), renameIdentifiers(set.exp, ctx))

    private fun visit(variable: ASTVariable, ctx: IdentifierMapping): ASTExpression =
        AST.variable(ctx.get(variable.name))

    private fun visit(lambda: ASTLambda, ctx: IdentifierMapping): ASTExpression {
        val newCtx = ctx.extend().sure()

        val v = freshVariable()
        newCtx.put(lambda.argument, v)

        return AST.lambda(v, renameIdentifiers(lambda.body, newCtx))
    }

    private fun visit(let: ASTLet, ctx: IdentifierMapping): ASTExpression {
        if (let.bindings.size() != 1) throw UnsupportedOperationException()

        val newCtx = ctx.extend()

        val v = freshVariable()
        newCtx.put(let.bindings.get(0).sure().name, v)

        val binding = ImplicitBinding(v, renameIdentifiers(let.bindings.get(0).sure().expr, ctx))

        return AST.let(false, binding, renameIdentifiers(let.body, newCtx)).sure()
    }

    private fun visit(let: ASTLetRec, ctx: IdentifierMapping): ASTExpression {
        if (let.bindings.size() != 1) throw UnsupportedOperationException();

        val newCtx = ctx.extend().sure()

        val v = freshVariable()
        newCtx.put(let.bindings.get(0).sure().name, v)

        val binding = ImplicitBinding(v, renameIdentifiers(let.bindings.get(0).sure().expr, newCtx))

        return AST.let(true, binding, renameIdentifiers(let.body, newCtx))
    }

    private fun visit(astCase: ASTCase, ctx: IdentifierMapping): ASTExpression {
        val alts = ArrayList<ASTAlternative>()

        for (val alt in astCase.alternatives) {
            val newCtx = ctx.extend()
            val pattern = renameIdentifiers(alt.sure().pattern, newCtx)
            alts.add(AST.alternative(pattern, renameIdentifiers(alt.sure().value, newCtx)))
        }

        return AST.caseExp(renameIdentifiers(astCase.exp, ctx), ImmutableList.copyOf(alts).sure())
    }
}
