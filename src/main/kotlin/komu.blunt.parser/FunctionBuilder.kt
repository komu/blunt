package komu.blunt.parser

import java.util.ArrayList
import komu.blunt.objects.Symbol
import komu.blunt.ast.ASTExpression
import komu.blunt.ast.ASTAlternative
import com.google.common.collect.ImmutableList
import komu.blunt.types.patterns.Pattern
import komu.blunt.ast.AST

import komu.blunt.objects.Symbol.symbol
import java.util.List
import komu.blunt.types.patterns.VariablePattern
import java.util.Collections.singletonList

class FunctionBuilder {
    private val symbols = ArrayList<Symbol?>()
    private val exps = ArrayList<ASTExpression?>()
    private val alternatives = ArrayList<ASTAlternative?>()

    fun addAlternative(args: ImmutableList<Pattern?>, body: ASTExpression?) {
        if (exps.isEmpty()) {
            for (val i in 0..args.size()-1) {
                val v = symbol("\$arg$i") // TODO: fresh symbols
                symbols.add(v.sure())
                exps.add(AST.variable(v.sure()))
            }
        } else if (args.size() != exps.size()) {
            throw SyntaxException("invalid amount of arguments")
        }

        alternatives.add(AST.alternative(Pattern.tuple(args), body).sure())
    }

    fun build(): ASTExpression {
        val alts = ImmutableList.copyOf(alternatives).sure()

        // optimization
        val simpleVars = containsOnlyVariablePatterns(alts)
        if (simpleVars != null)
            return AST.lambda(simpleVars, alts.get(0)?.value.sure())

        return AST.lambda(symbols, AST.caseExp(AST.tuple(exps), alts));
    }

    fun containsOnlyVariablePatterns(alts: ImmutableList<ASTAlternative?>): List<Symbol?>? {
        if (alts.size() == 1)
            return variablePattern(alts.get(0)?.pattern.sure())
        return null
    }

    private fun variablePattern(pattern: Pattern): List<Symbol?>? {
        if (pattern is VariablePattern)
            return singletonList<Symbol?>(pattern.`var`.sure())

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

