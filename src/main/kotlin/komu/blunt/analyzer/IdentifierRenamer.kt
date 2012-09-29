package komu.blunt.analyzer

import komu.blunt.ast.*
import komu.blunt.types.patterns.*

/**
 * Walks the AST to perform α-conversion on all expressions. After performing this conversion, the
 * further optimization and analyzation passes need not know about scoping rules but can assume that
 * same name always refers to same variable.
 */

private fun IdentifierMapping.rename(exp: ASTExpression): ASTExpression =
    when (exp) {
        is ASTConstant    -> exp
        is ASTConstructor -> exp
        is ASTSequence    -> exp.map { rename(it) }
        is ASTApplication -> exp.map { rename(it) }
        is ASTSet         -> ASTSet(this[exp.variable], rename(exp.exp))
        is ASTVariable    -> ASTVariable(this[exp.name])
        is ASTLet         -> renameLet(false, exp.bindings, exp.body)
        is ASTLetRec      -> renameLet(true, exp.bindings, exp.body)
        is ASTLambda      -> renameLambda(exp)
        is ASTCase        -> renameCase(exp)
        else              -> throw AnalyzationException("unknown expression '$exp'")
    }

private fun IdentifierMapping.renameLet(recursive: Boolean,
                                        bindings: List<ImplicitBinding>,
                                        body: ASTExpression): ASTExpression {
    val newCtx = extend()
    val bindingExprCtx = if (recursive) newCtx else this

    val newBindings = bindings.map { binding ->
        val v = newCtx.freshMappingFor(binding.name)
        ImplicitBinding(v, bindingExprCtx.rename(binding.expr))
    }

    val newBody = newCtx.rename(body)

    return if (recursive)
        ASTLetRec(newBindings, newBody)
    else
        ASTLet(newBindings, newBody)
}

private fun IdentifierMapping.renameLambda(lambda: ASTLambda): ASTExpression {
    val newCtx = extend()
    val fresh = newCtx.freshMappingFor(lambda.argument)
    return ASTLambda(fresh, newCtx.rename(lambda.body))
}

private fun IdentifierMapping.renameCase(astCase: ASTCase) =
    ASTCase(rename(astCase.exp), astCase.alternatives.map { renameAlternative(it) })

private fun IdentifierMapping.renameAlternative(alt: ASTAlternative): ASTAlternative {
    val newCtx = extend()
    return ASTAlternative(newCtx.renamePattern(alt.pattern), newCtx.rename(alt.value))
}

private fun IdentifierMapping.renamePattern(pattern: Pattern): Pattern =
    when (pattern) {
        is WildcardPattern    -> pattern
        is LiteralPattern     -> pattern
        is VariablePattern    -> Pattern.variable(freshMappingFor(pattern.variable))
        is ConstructorPattern -> pattern.map { renamePattern(it) }
        else                  -> throw AnalyzationException("invalid pattern '$pattern'")
    }
