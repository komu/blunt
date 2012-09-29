package komu.blunt.analyzer

import komu.blunt.ast.*
import komu.blunt.objects.Symbol
import komu.blunt.types.patterns.*
import komu.blunt.utils.Sequence

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

    private val mappings = hashMap<Symbol,Symbol>()
    private val sequence: Sequence = if (parent != null) parent.seq() else Sequence()

    private fun seq() = sequence

    fun get(v: Symbol): Symbol {
        var mapping = this

        while (true) {
            val sym = mapping.mappings[v]
            if (sym != null)
                return sym

            val parent = mapping.parent
            if (parent == null)
                break
            else
                mapping = parent
        }

        return v
    }

    fun set(oldName: Symbol, newName: Symbol) {
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
}

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
    val newCtx = createChildContext()
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
    val newCtx = createChildContext()
    val fresh = newCtx.freshMappingFor(lambda.argument)
    return ASTLambda(fresh, newCtx.rename(lambda.body))
}

private fun IdentifierMapping.renameCase(astCase: ASTCase) =
    ASTCase(rename(astCase.exp), astCase.alternatives.map { renameAlternative(it) })

private fun IdentifierMapping.renameAlternative(alt: ASTAlternative): ASTAlternative {
    val newCtx = createChildContext()
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
