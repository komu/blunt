package komu.blunt.analyzer

import komu.blunt.ast.ASTAlternative
import komu.blunt.ast.ASTExpression
import komu.blunt.ast.ImplicitBinding
import komu.blunt.objects.Symbol
import komu.blunt.types.patterns.Pattern
import komu.blunt.utils.Sequence
import java.util.*

/**
 * Walks the AST to perform α-conversion on all expressions. After performing this conversion, the
 * further optimization and analyzation passes need not know about scoping rules but can assume that
 * same name always refers to same variable.
 */
fun renameIdentifiers(exp: ASTExpression) =
    IdentifierMapping().rename(exp)

/**
 * Mapping from old identifiers to new identifiers.
 */
class IdentifierMapping(val parent: IdentifierMapping? = null) {

    private val mappings = HashMap<Symbol,Symbol>()
    private val sequence: Sequence = if (parent != null) parent.seq() else Sequence()

    private fun seq() = sequence

    operator fun get(v: Symbol): Symbol =
        mappings[v] ?: parent?.get(v) ?: v

    operator fun set(oldName: Symbol, newName: Symbol) {
        val old = mappings.put(oldName, newName)
        if (old != null)
            throw IllegalArgumentException("duplicate mapping for '$oldName'");
    }

    fun createChildContext() = IdentifierMapping(this)

    /**
     * Creates a new unique symbol based on name, installs it on the mapping and returns it.
     */
    fun freshMappingFor(name: Symbol): Symbol {
        val v = sequence.nextSymbol("\$${name}_")
        this[name] = v
        return v
    }

    fun rename(exp: ASTExpression): ASTExpression = when (exp) {
        is ASTExpression.Constant       -> exp
        is ASTExpression.Constructor    -> exp
        is ASTExpression.Sequence       -> exp.map { rename(it) }
        is ASTExpression.Application    -> exp.map { rename(it) }
        is ASTExpression.Set            -> ASTExpression.Set(this[exp.variable], rename(exp.exp))
        is ASTExpression.Variable       -> ASTExpression.Variable(this[exp.name])
        is ASTExpression.Let            -> renameLet(exp.bindings, exp.body, recursive = false)
        is ASTExpression.LetRec         -> renameLet(exp.bindings, exp.body, recursive = true)
        is ASTExpression.Lambda         -> renameLambda(exp)
        is ASTExpression.Case           -> renameCase(exp)
    }

    private fun renameLet(bindings: List<ImplicitBinding>,
                          body: ASTExpression,
                          recursive: Boolean): ASTExpression {
        val newCtx = createChildContext()
        val bindingExprCtx = if (recursive) newCtx else this

        val newBindings = bindings.map { binding ->
            val v = newCtx.freshMappingFor(binding.name)
            ImplicitBinding(v, bindingExprCtx.rename(binding.expr))
        }

        val newBody = newCtx.rename(body)

        return if (recursive)
            ASTExpression.LetRec(newBindings, newBody)
        else
            ASTExpression.Let(newBindings, newBody)
    }

    private fun renameLambda(lambda: ASTExpression.Lambda): ASTExpression {
        val newCtx = createChildContext()
        val fresh = newCtx.freshMappingFor(lambda.argument)
        return ASTExpression.Lambda(fresh, newCtx.rename(lambda.body))
    }

    private fun renameCase(case: ASTExpression.Case) =
        ASTExpression.Case(rename(case.exp), case.alternatives.map { renameAlternative(it) })

    private fun renameAlternative(alt: ASTAlternative): ASTAlternative {
        val newCtx = createChildContext()
        return ASTAlternative(newCtx.renamePattern(alt.pattern), newCtx.rename(alt.value))
    }

    private fun renamePattern(pattern: Pattern): Pattern = when (pattern) {
        is Pattern.Wildcard     -> pattern
        is Pattern.Literal      -> pattern
        is Pattern.Variable     -> Pattern.Variable(freshMappingFor(pattern.variable))
        is Pattern.Constructor  -> pattern.map { renamePattern(it) }
    }
}
