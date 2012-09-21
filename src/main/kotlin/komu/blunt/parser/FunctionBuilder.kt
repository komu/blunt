package komu.blunt.parser

import java.util.ArrayList
import java.util.Collections.singletonList
import komu.blunt.ast.AST
import komu.blunt.ast.ASTAlternative
import komu.blunt.ast.ASTExpression
import komu.blunt.objects.Symbol
import komu.blunt.types.patterns.Pattern
import komu.blunt.types.patterns.VariablePattern

class FunctionBuilder {
    private val symbols = arrayList<Symbol>()
    private val exps = arrayList<ASTExpression>()
    private val alternatives = arrayList<ASTAlternative>()

    fun addAlternative(args: List<Pattern>, body: ASTExpression) {
        if (exps.isEmpty()) {
            for (val i in args.indices) {
                val v = Symbol("\$arg$i") // TODO: fresh symbols
                symbols.add(v)
                exps.add(AST.variable(v))
            }
        } else if (args.size != exps.size) {
            throw SyntaxException("invalid amount of arguments")
        }

        alternatives.add(AST.alternative(Pattern.tuple(args), body))
    }

    fun build(): ASTExpression {
        val alts = ArrayList(alternatives)

        // optimization
        val simpleVars = containsOnlyVariablePatterns(alts)
        if (simpleVars != null)
            return AST.lambda(simpleVars, alts.first().value)

        return AST.lambda(symbols, AST.caseExp(AST.tuple(exps), alts))
    }

    fun containsOnlyVariablePatterns(alts: List<ASTAlternative>): List<Symbol>? {
        if (alts.size == 1)
            return variablePattern(alts.first().pattern)
        return null
    }

    private fun variablePattern(pattern: Pattern): List<Symbol>? {
        if (pattern is VariablePattern)
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

