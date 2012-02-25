package komu.blunt.parser

import com.google.common.collect.ImmutableList
import komu.blunt.ast.*
import komu.blunt.objects.Symbol
import komu.blunt.types.DataTypeDefinitions
import komu.blunt.types.ConstructorNames
import komu.blunt.types.patterns.Pattern
import komu.blunt.types.patterns.VariablePattern

import java.util.ArrayList
import java.util.List

import java.util.Collections.singletonList
import komu.blunt.parser.TokenType.*
import java.util.Arrays
import std.util.*

class Parser(source: String) {

    private val operators = OperatorSet()
    private val lexer = Lexer(source, operators)
    private val typeParser = TypeParser(lexer)
    private val patternParser = PatternParser(lexer)
    private val dataTypeParser = DataTypeParser(lexer, typeParser)

    private val EXPRESSION_START_TOKENS =
        arrayList(IF, LET, LAMBDA, LPAREN, LBRACKET, LITERAL, IDENTIFIER, TYPE_OR_CTOR_NAME, CASE)

    class object {
        fun parseExpression(source: String) =
            Parser(source).parseExpression()
    }

    fun parseDefinitions(): List<ASTDefinition> {
        val result = ArrayList<ASTDefinition>()

        while (lexer.hasMoreTokens())
            result.add(parseDefinition())

        return result
    }

    fun parseDefinition(): ASTDefinition =
        if (lexer.nextTokenIs(DATA))
            dataTypeParser.parseDataDefinition()
        else
            parseValueDefinition()

    private fun parseValueDefinition(): ASTValueDefinition {
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

    private fun parseSimpleDefinition(): ASTValueDefinition {
        lexer.pushBlockStartAtNextToken()

        val name = parseIdentifier()

        lexer.expectToken(ASSIGN)
        val value = parseExpression()
        lexer.expectToken(END)
        return AST.define(name, value)
    }

    // <pattern> <op> <pattern> = <exp> ;;
    private fun parseOperatorDefinition(): ASTValueDefinition {
        lexer.pushBlockStartAtNextToken()

        val left = patternParser.parseSimplePattern()
        val op = lexer.readTokenValue(OPERATOR)
        val right = patternParser.parseSimplePattern()

        lexer.expectToken(ASSIGN)

        val value = parseExpression()
        lexer.expectToken(END)

        val functionBuilder = FunctionBuilder()
        functionBuilder.addAlternative(ImmutableList.of<Pattern>(left, right).sure(), value)
        return AST.define(op.toSymbol(), functionBuilder.build())
    }

    // <ident> <pattern>+ = <exp> ;;
    private fun parseNormalValueDefinition(): ASTValueDefinition {
        val functionBuilder = FunctionBuilder()

        var name: Symbol? = null
        while (name == null || nextTokenIsIdentifier(name.sure())) {
            lexer.pushBlockStartAtNextToken()
            name = parseIdentifier()

            val args = ArrayList<Pattern>()

            while (!lexer.nextTokenIs(ASSIGN))
                args.add(patternParser.parseSimplePattern())

            lexer.expectToken(ASSIGN)

            val value = parseExpression()
            lexer.expectToken(END)
            functionBuilder.addAlternative(ImmutableList.copyOf(args).sure(), value)
        }

        return AST.define(name.sure(), functionBuilder.build())
    }

    private fun nextTokenIsIdentifier(name: Symbol) =
        lexer.nextTokenIs(IDENTIFIER) && lexer.peekTokenValue(IDENTIFIER) == name.toString()

    public fun parseExpression(): ASTExpression {
        var exp = parseExp(0)

        while (true) {
            if (lexer.readMatchingToken(SEMICOLON)) {
                val rhs = parseExp(0)
                exp = AST.sequence(exp, rhs)
            } else {
                return exp
            }
        }

        throw AssertionError("unreached")
    }

    private fun parseExp(level: Int): ASTExpression =
        if (level <= operators.maxLevel)
            parseExpN(level)
        else
            parseApplicative()

    private fun parseExpN(level: Int): ASTExpression {
        var exp = parseExp(level + 1)

        while (true) {
            val op = lexer.readOperatorMatchingLevel(level)
            if (op != null) {
                val rhs = parseExp(if (op.associativity == Associativity.LEFT) level+1 else level)
                exp = binary(op, exp, rhs)
            } else {
                return exp
            }
        }

        throw AssertionError("unreached")
    }

    private fun parseApplicative(): ASTExpression {
        var exp = parsePrimitive()

        while (lexer.nextTokenIsOneOf(EXPRESSION_START_TOKENS))
            exp = AST.apply(exp, parsePrimitive())

        return exp
    }

    private fun parsePrimitive(): ASTExpression =
        when (lexer.peekTokenType()) {
            EOF      -> throw parseError("unexpected eof")
            IF       -> parseIf()
            LET      -> parseLet()
            LAMBDA   -> parseLambda()
            CASE     -> parseCase()
            LPAREN   -> parseParens()
            LBRACKET -> parseList()
            LITERAL  -> AST.constant(lexer.readTokenValue(LITERAL))
            else     -> parseVariableOrConstructor()
        }

    // if <expr> then <expr> else <expr>
    private fun parseIf(): ASTExpression {
        lexer.expectToken(IF)
        val test = parseExpression()
        lexer.expectToken(THEN)
        val cons = parseExpression()
        lexer.expectToken(ELSE)
        val alt = parseExpression()

        return AST.ifExp(test, cons, alt)
    }

    // case <exp> of <alternative>+
    private fun parseCase(): ASTExpression {
        lexer.expectIndentStartToken(CASE)
        val exp = parseExpression()
        lexer.expectToken(OF)

        val alts = ArrayList<ASTAlternative>()
        do {
            alts.add(parseAlternative())
        } while (!lexer.nextTokenIs(END))
        lexer.expectToken(END)

        return AST.caseExp(exp, ImmutableList.copyOf(alts).sure())
    }

    private fun parseAlternative(): ASTAlternative {
        val pattern = patternParser.parsePattern()
        lexer.expectIndentStartToken(RIGHT_ARROW)
        val exp = parseExpression()
        lexer.expectToken(END)
        return AST.alternative(pattern, exp)
    }

    // let [rec] <ident> <ident>* = <expr> in <expr>
    private fun parseLet(): ASTExpression {
        lexer.expectToken(LET)
        val recursive = lexer.readMatchingToken(REC)

        var name = parseIdentifier()

        val args = ArrayList<Symbol>()

        if (lexer.nextTokenIs(OPERATOR)) {
            val op = lexer.readTokenValue(OPERATOR)
            args.add(name)
            args.add(parseIdentifier())
            name = op.toSymbol()

            lexer.expectToken(ASSIGN)
        } else {
            while (!lexer.readMatchingToken(ASSIGN))
                args.add(parseIdentifier())
        }

        var value = parseExpression()
        lexer.expectToken(IN)
        val body = parseExpression()

        if (!args.isEmpty())
            value = AST.lambda(args, value)

        return AST.let(recursive, ImplicitBinding(name, value), body)
    }

    // \ <ident> -> expr
    private fun parseLambda(): ASTExpression {
        lexer.expectToken(LAMBDA)

        val args = ArrayList<Pattern>()

        do {
            args.add(patternParser.parseSimplePattern())
        } while (!lexer.readMatchingToken(RIGHT_ARROW))

        val builder = FunctionBuilder()
        builder.addAlternative(ImmutableList.copyOf(args).sure(), parseExpression())
        return builder.build()
    }

    // () | (<op>) | ( <expr> )
    private fun parseParens(): ASTExpression {
        lexer.expectToken(LPAREN)
        if (lexer.readMatchingToken(RPAREN))
            return AST.constructor(ConstructorNames.UNIT.sure())

        if (lexer.nextTokenIs(OPERATOR)) {
            val op = lexer.readTokenValue(OPERATOR)
            lexer.expectToken(RPAREN)

            return operatorExp(op)
        }

        val exps = ArrayList<ASTExpression>()

        do {
            exps.add(parseExpression())
        } while (lexer.readMatchingToken(COMMA))

        lexer.expectToken(RPAREN)

        if (exps.size == 1)
            return exps.first()
        else
            return AST.tuple(exps)
    }

    // []
    // [<exp> (,<exp>)*]
    private fun parseList(): ASTExpression {
        val list = AST.listBuilder()

        lexer.expectToken(LBRACKET)

        if (!lexer.nextTokenIs(RBRACKET)) {
            do {
                list.add(parseExpression())
            } while (lexer.readMatchingToken(COMMA))
        }

        lexer.expectToken(RBRACKET)

        return list.build()
    }

    private fun parseVariableOrConstructor(): ASTExpression {
        if (lexer.nextTokenIs(IDENTIFIER))
            return AST.variable(lexer.readTokenValue(IDENTIFIER))

        if (lexer.nextTokenIs(TYPE_OR_CTOR_NAME))
            return AST.constructor(lexer.readTokenValue(TYPE_OR_CTOR_NAME))

        if (lexer.readMatchingToken(LPAREN)) {
            val op = lexer.readTokenValue(OPERATOR)
            lexer.expectToken(RPAREN)

            return operatorExp(op)
        }

        throw lexer.expectFailure("identifier or type constructor")
    }

    private fun parseIdentifier(): Symbol {
        if (lexer.nextTokenIs(IDENTIFIER))
            return Symbol(lexer.readTokenValue(IDENTIFIER))

        if (lexer.readMatchingToken(LPAREN)) {
            val op = lexer.readTokenValue(OPERATOR)
            lexer.expectToken(RPAREN)
            return op.toSymbol()
        }

        throw lexer.expectFailure("identifier")
    }

    private fun parseError(s: String): SyntaxException =
        lexer.parseError(s)

    private fun binary(op: Operator, lhs: ASTExpression, rhs: ASTExpression): ASTExpression {
        return AST.apply(operatorExp(op), lhs, rhs)
    }

    private fun operatorExp(op: Operator): ASTExpression =
        if (op.isConstructor) AST.constructor(op.toString()) else AST.variable(op.toSymbol())
}
