package komu.blunt.parser

import komu.blunt.ast.AST
import komu.blunt.ast.ASTAlternative
import komu.blunt.ast.ASTExpression
import komu.blunt.objects.Symbol
import komu.blunt.types.patterns.Pattern
import java.util.*
import java.util.Collections.singletonList

fun buildFunction(args: List<Pattern>, body: ASTExpression): ASTExpression {
    val builder = FunctionBuilder()
    builder.addAlternative(args, body)
    return builder.build()
}

class FunctionBuilder {
    private val symbols = ArrayList<Symbol>()
    private val exps = ArrayList<ASTExpression>()
    private val alternatives = ArrayList<ASTAlternative>()

    fun addAlternative(args: List<Pattern>, body: ASTExpression) {
        if (exps.isEmpty()) {
            for (i in args.indices) {
                val v = Symbol("\$arg$i") // TODO: fresh symbols
                symbols.add(v)
                exps.add(ASTExpression.Variable(v))
            }
        } else if (args.size != exps.size) {
            throw SyntaxException("invalid amount of arguments")
        }

        alternatives.add(ASTAlternative(Pattern.tuple(args), body))
    }

    fun build(): ASTExpression {
        val alts = ArrayList(alternatives)

        // optimization
        val simpleVars = containsOnlyVariablePatterns(alts)
        return if (simpleVars != null)
            AST.lambda(simpleVars, alts.first().value)
        else
            AST.lambda(symbols, ASTExpression.Case(ASTExpression.tuple(exps), alts))
    }

    fun containsOnlyVariablePatterns(alts: List<ASTAlternative>): List<Symbol>? {
        if (alts.size == 1)
            return variablePattern(alts.first().pattern)
        return null
    }

    private fun variablePattern(pattern: Pattern): List<Symbol>? {
        if (pattern is Pattern.Variable)
            return singletonList(pattern.variable)

        /*
        if (pattern instanceof ConstructorPattern) {
            ConstructorPattern c = (ConstructorPattern) pattern;

            if (c.name.equals("()"))
                return null;
            if (!c.name.equals(DataTypeDefinitions.tupleName(c.args.size())))
                return null;

            List<Symbol> vars = new ArrayList<>(c.args.size());

            for (Pattern p : c.args)
                if (p instanceof VariablePattern) {
                    vars.add(((VariablePattern) p).var);
                } else {
                    return null;
                }

            return vars;
        }
        */

        return null
    }
}

