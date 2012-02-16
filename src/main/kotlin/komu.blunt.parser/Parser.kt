package komu.blunt.parser

import com.google.common.collect.ImmutableList
import komu.blunt.ast.*
import komu.blunt.objects.Symbol
import komu.blunt.types.DataTypeDefinitions
import komu.blunt.types.patterns.Pattern
import komu.blunt.types.patterns.VariablePattern

import java.util.ArrayList
import java.util.List

import java.util.Arrays.asList
import java.util.Collections.singletonList
import komu.blunt.objects.Symbol.symbol
import komu.blunt.parser.Associativity.LEFT
import komu.blunt.parser.TokenType.*

class Parser(source: String) {

    private val operators = OperatorSet()
    private val lexer = Lexer(source, operators)
    private val typeParser = TypeParser(lexer)
    private val patternParser = PatternParser(lexer)
    private val dataTypeParser = DataTypeParser(lexer, typeParser)

    fun isExpressionStartToken(token: TokenType<out Any?>?) =
        (token == IF || token == LET || token == LAMBDA
          || token == LPAREN || token == LBRACKET || token == LITERAL
          || token == IDENTIFIER || token == TYPE_OR_CTOR_NAME ||token == CASE)

    class object {
        fun parseExpression(source: String) =
            Parser(source).parseExpression()
    }

    fun parseDefinitions(): List<ASTDefinition?>? {
        val result = ArrayList<ASTDefinition?>()

        while (lexer.hasMoreTokens())
            result.add(parseDefinition())

        return result
    }

    fun parseDefinition(): ASTDefinition? =
        if (lexer.nextTokenIs(DATA))
            dataTypeParser.parseDataDefinition().sure()
        else
            parseValueDefinition()

    private fun parseValueDefinition(): ASTValueDefinition? {
        val lexerState = lexer.save()
        try {
            try {
                return parseSimpleDefinition()
            } catch (e: SyntaxException) {
                lexer.restore(lexerState)
                return parseOperatorDefinition()
            }

        } catch (e: SyntaxException) {
            lexer.restore(lexerState)
            return parseNormalValueDefinition()
        }
    }

    private fun parseSimpleDefinition(): ASTValueDefinition? {
        lexer.pushBlockStartAtNextToken()

        val name = parseIdentifier()

        lexer.expectToken(ASSIGN)
        val value = parseExpression()
        lexer.expectToken(END)
        return AST.define(name, value)
    }

    // <pattern> <op> <pattern> = <exp> ;;
    private fun parseOperatorDefinition(): ASTValueDefinition? {
        lexer.pushBlockStartAtNextToken()

        val left = patternParser.parseSimplePattern()
        val op = lexer.readTokenValue(OPERATOR)
        val right = patternParser.parseSimplePattern()

        lexer.expectToken(ASSIGN)

        val value = parseExpression()
        lexer.expectToken(END)

        val functionBuilder = FunctionBuilder()
        functionBuilder.addAlternative(ImmutableList.of<Pattern?>(left, right).sure(), value)
        return AST.define(op?.toSymbol(), functionBuilder.build())
    }

    // <ident> <pattern>+ = <exp> ;;
    private fun parseNormalValueDefinition(): ASTValueDefinition? {
        val functionBuilder = FunctionBuilder()

        var name: Symbol? = null
        while (name == null || nextTokenIsIdentifier(name.sure())) {
            lexer.pushBlockStartAtNextToken()
            name = parseIdentifier()

            val args = ArrayList<Pattern?>()

            while (!lexer.nextTokenIs(ASSIGN))
                args.add(patternParser.parseSimplePattern())

            lexer.expectToken(ASSIGN)

            val value = parseExpression()
            lexer.expectToken(END)
            functionBuilder.addAlternative(ImmutableList.copyOf(args).sure(), value)
        }

        return AST.define(name, functionBuilder.build()).sure()
    }

    private fun nextTokenIsIdentifier(name: Symbol) =
        lexer.nextTokenIs(IDENTIFIER) && lexer.peekTokenValue(IDENTIFIER) == name.toString()

    public fun parseExpression(): ASTExpression? {
        var exp = parseExp(0)

        while (true) {
            if (lexer.readMatchingToken(SEMICOLON)) {
                val rhs = parseExp(0)
                exp = AST.sequence(exp, rhs).sure()
            } else {
                return exp
            }
        }

        throw AssertionError("unreached")
    }

    private fun parseExp(level: Int): ASTExpression? =
        if (level <= operators.getMaxLevel())
            parseExpN(level)
        else
            parseApplicative()

    private fun parseExpN(level: Int): ASTExpression? {
        var exp = parseExp(level + 1)

        while (true) {
            val op = lexer.readOperatorMatchingLevel(level)
            if (op != null) {
                val rhs = parseExp(if (op.associativity == LEFT) level+1 else level)
                exp = binary(op, exp, rhs)
            } else {
                return exp
            }
        }

        throw AssertionError("unreached")
    }

    private fun parseApplicative(): ASTExpression? {
        var exp = parsePrimitive()

        while (isExpressionStartToken(lexer.peekTokenType()))
            exp = AST.apply(exp, parsePrimitive())

        return exp
    }

    private fun parsePrimitive(): ASTExpression? {
        val typ = lexer.peekTokenType()

        if (typ == EOF)
            throw parseError("unexpected eof")

        if (typ == IF)
            return parseIf()

        if (typ == LET)
            return parseLet()

        if (typ == LAMBDA)
            return parseLambda()

        if (typ == CASE)
            return parseCase()

        if (typ == LPAREN)
            return parseParens().sure()

        if (typ == LBRACKET)
            return parseList()

        if (typ == LITERAL)
            return AST.constant(lexer.readTokenValue(LITERAL)).sure()

        return parseVariableOrConstructor().sure()
    }

    // if <expr> then <expr> else <expr>
    private fun parseIf(): ASTExpression? {
        lexer.expectToken(IF)
        val test = parseExpression()
        lexer.expectToken(THEN)
        val cons = parseExpression()
        lexer.expectToken(ELSE)
        val alt = parseExpression()

        return AST.ifExp(test, cons, alt).sure()
    }

    // case <exp> of <alternative>+
    private fun parseCase(): ASTExpression? {
        lexer.expectIndentStartToken(CASE)
        val exp = parseExpression()
        lexer.expectToken(OF)

        val alts = ArrayList<ASTAlternative?>()
        do {
            alts.add(parseAlternative())
        } while (!lexer.nextTokenIs(END))
        lexer.expectToken(END)

        return AST.caseExp(exp, ImmutableList.copyOf(alts))
    }

    private fun parseAlternative(): ASTAlternative? {
        val pattern = patternParser.parsePattern()
        lexer.expectIndentStartToken(RIGHT_ARROW)
        val exp = parseExpression()
        lexer.expectToken(END)
        return AST.alternative(pattern, exp)
    }

    // let [rec] <ident> <ident>* = <expr> in <expr>
    private fun parseLet(): ASTExpression? {
        lexer.expectToken(LET)
        val recursive = lexer.readMatchingToken(REC)

        var name = parseIdentifier()

        val args = ArrayList<Symbol?>()

        if (lexer.nextTokenIs(OPERATOR)) {
            val op = lexer.readTokenValue(OPERATOR)
            args.add(name)
            args.add(parseIdentifier())
            name = symbol(op.toString()).sure()

            lexer.expectToken(ASSIGN)
        } else {
            while (!lexer.readMatchingToken(ASSIGN))
                args.add(parseIdentifier());
        }

        var value = parseExpression()
        lexer.expectToken(IN)
        val body = parseExpression()

        if (!args.isEmpty())
            value = AST.lambda(args, value).sure()

        return AST.let(recursive, ImplicitBinding(name, value), body).sure()
    }

    // \ <ident> -> expr
    private fun parseLambda(): ASTExpression? {
        lexer.expectToken(LAMBDA)

        val args = ArrayList<Pattern?>()

        do {
            args.add(patternParser.parseSimplePattern())
        } while (!lexer.readMatchingToken(RIGHT_ARROW))

        val builder = FunctionBuilder()
        builder.addAlternative(ImmutableList.copyOf(args).sure(), parseExpression())
        return builder.build().sure()
    }

    // () | (<op>) | ( <expr> )
    private fun parseParens(): ASTExpression? {
        lexer.expectToken(LPAREN)
        if (lexer.readMatchingToken(RPAREN))
            return AST.constructor(DataTypeDefinitions.UNIT).sure()

        if (lexer.nextTokenIs(OPERATOR)) {
            val op = lexer.readTokenValue(OPERATOR).sure()
            lexer.expectToken(RPAREN)

            return (if (op.isConstructor) AST.constructor(op.toString()) else AST.variable(op.toString())).sure()
        }

        val exps = ArrayList<ASTExpression?>()

        do {
            exps.add(parseExpression())
        } while (lexer.readMatchingToken(COMMA))

        lexer.expectToken(RPAREN)

        if (exps.size() == 1)
            return exps.get(0).sure()
        else
            return AST.tuple(exps).sure()
    }

    // []
    // [<exp> (,<exp>)*]
    private fun parseList(): ASTExpression? {
        val list = AST.listBuilder()

        lexer.expectToken(LBRACKET)

        if (!lexer.nextTokenIs(RBRACKET)) {
            do {
                list?.add(parseExpression())
            } while (lexer.readMatchingToken(COMMA))
        }

        lexer.expectToken(RBRACKET)

        return list?.build().sure()
    }

    private fun parseVariableOrConstructor(): ASTExpression? {
        if (lexer.nextTokenIs(IDENTIFIER))
            return AST.variable(lexer.readTokenValue(IDENTIFIER))

        if (lexer.nextTokenIs(TYPE_OR_CTOR_NAME))
            return AST.constructor(lexer.readTokenValue(TYPE_OR_CTOR_NAME))

        if (lexer.readMatchingToken(LPAREN)) {
            val op = lexer.readTokenValue(OPERATOR).sure()
            lexer.expectToken(RPAREN)

            if (op.isConstructor)
                return AST.constructor(op.toString())
            else
                return AST.variable(op.toString())
        }

        throw lexer.expectFailure("identifier or type constructor");
    }

    private fun parseIdentifier(): Symbol? {
        if (lexer.nextTokenIs(IDENTIFIER)) {
            return symbol(lexer.readTokenValue(IDENTIFIER)).sure()
        }

        if (lexer.readMatchingToken(LPAREN)) {
            val op = lexer.readTokenValue(OPERATOR)
            lexer.expectToken(RPAREN)
            return op?.toSymbol().sure()
        }

        throw lexer.expectFailure("identifier");
    }

    private fun parseError(s: String): SyntaxException =
        lexer.parseError(s).sure()

    private fun binary(op: Operator, lhs: ASTExpression?, rhs: ASTExpression?): ASTExpression? {
        val exp = if (op.isConstructor) AST.constructor(op.toString()) else AST.variable(op.toString())

        return AST.apply(exp, lhs, rhs).sure()
    }
}
