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
class IdentifierRenamer : ASTVisitor<IdentifierMapping, ASTExpression>, PatternVisitor<IdentifierMapping, Pattern> {

    private var sequence = 1

    class object {
        fun rename(exp: ASTExpression): ASTExpression =
            IdentifierRenamer().renameIdentifiers(exp, IdentifierMapping())
    }

    fun renameIdentifiers(exp: ASTExpression?, ctx: IdentifierMapping) =
        exp.accept(this, ctx)

    fun renameIdentifiers(exp: Pattern?, ctx: IdentifierMapping) =
        exp.accept(this, ctx)

    private fun freshVariable() =
        Symbol.symbol("\$var${sequence++}")

    override fun visit(sequence: ASTSequence?, ctx: IdentifierMapping): ASTExpression {
        val result = AST.sequenceBuilder().sure()

        for (val exp in sequence?.exps)
            result.add(renameIdentifiers(exp, ctx))

        return result.build().sure()
    }

    override fun visit(application: ASTApplication?, ctx: IdentifierMapping): ASTExpression =
        AST.apply(renameIdentifiers(application?.func, ctx), renameIdentifiers(application?.arg, ctx)).sure()

    override fun visit(constant: ASTConstant?, ctx: IdentifierMapping): ASTExpression =
        constant.sure()

    override fun visit(set: ASTSet?, ctx: IdentifierMapping): ASTExpression =
        AST.set(ctx.get(set?.`var`), renameIdentifiers(set?.exp, ctx)).sure()

    override fun visit(variable: ASTVariable?, ctx: IdentifierMapping): ASTExpression =
        AST.variable(ctx.get(variable?.`var`)).sure()

    override fun visit(lambda: ASTLambda?, ctx: IdentifierMapping): ASTExpression {
        val newCtx = ctx.extend().sure()

        val v = freshVariable()
        newCtx.put(lambda?.argument, v)

        return AST.lambda(v, renameIdentifiers(lambda?.body, newCtx)).sure()
    }

    override fun visit(let: ASTLet?, ctx: IdentifierMapping): ASTExpression {
        if (let?.bindings?.size() != 1) throw UnsupportedOperationException()

        val newCtx = ctx.extend().sure()

        val v = freshVariable()
        newCtx.put(let?.bindings?.get(0)?.name, v)

        val binding = ImplicitBinding(v, renameIdentifiers(let?.bindings?.get(0)?.expr, ctx))

        return AST.let(false, binding, renameIdentifiers(let?.body, newCtx)).sure()
    }

    override fun visit(let: ASTLetRec?, ctx: IdentifierMapping): ASTExpression {
        if (let?.bindings?.size() != 1) throw UnsupportedOperationException();

        val newCtx = ctx.extend().sure()

        val v = freshVariable()
        newCtx.put(let?.bindings?.get(0)?.name, v)

        val binding = ImplicitBinding(v, renameIdentifiers(let?.bindings?.get(0)?.expr, newCtx))

        return AST.let(true, binding, renameIdentifiers(let?.body, newCtx)).sure()
    }

    override fun visit(constructor: ASTConstructor?, ctx: IdentifierMapping): ASTExpression =
        constructor.sure()

    override fun visit(astCase: ASTCase?, ctx: IdentifierMapping): ASTExpression {
        val alts = ArrayList<ASTAlternative?>()

        for (val alt in astCase?.alternatives) {
            val newCtx = ctx.extend().sure()
            val pattern = renameIdentifiers(alt?.pattern, newCtx);
            alts.add(AST.alternative(pattern, renameIdentifiers(alt?.value, newCtx)))
        }

        return AST.caseExp(renameIdentifiers(astCase?.exp, ctx), ImmutableList.copyOf(alts)).sure()
    }

    override fun visit(pattern: ConstructorPattern?, ctx: IdentifierMapping): Pattern {
        val args = ArrayList<Pattern?>()

        for (val arg in pattern?.args)
            args.add(renameIdentifiers(arg, ctx));

        return Pattern.constructor(pattern?.name, ImmutableList.copyOf(args)).sure()
    }

    override fun visit(pattern: LiteralPattern?, ctx: IdentifierMapping): Pattern =
        pattern.sure()

    override fun visit(pattern: VariablePattern?, ctx: IdentifierMapping): Pattern {
        val v = freshVariable()
        ctx.put(pattern?.`var`, v)
        return Pattern.variable(v).sure()
    }

    override fun visit(pattern: WildcardPattern?, ctx: IdentifierMapping): Pattern =
        pattern.sure()
}


