package komu.blunt.parser

import komu.blunt.ast.*
import komu.blunt.objects.Symbol
import komu.blunt.parser.TokenType.Companion.CASE
import komu.blunt.parser.TokenType.Companion.EOF
import komu.blunt.parser.TokenType.Companion.IF
import komu.blunt.parser.TokenType.Companion.LAMBDA
import komu.blunt.parser.TokenType.Companion.LBRACKET
import komu.blunt.parser.TokenType.Companion.LET
import komu.blunt.parser.TokenType.Companion.LITERAL
import komu.blunt.parser.TokenType.Companion.LPAREN
import komu.blunt.types.ConstructorNames
import komu.blunt.types.patterns.Pattern
import java.util.*

fun parseExpression(source: String) =
    Parser(source).parseExpression()

class Parser(source: String) {

    private val operators = OperatorSet()
    private val lexer = Lexer(source, operators)
    private val typeParser = TypeParser(lexer)
    private val patternParser = PatternParser(lexer)
    private val dataTypeParser = DataTypeParser(lexer, typeParser)

    private val EXPRESSION_START_TOKENS =
        listOf(IF, LET, LAMBDA, LPAREN, LBRACKET, LITERAL, TokenType.IDENTIFIER, TokenType.TYPE_OR_CTOR_NAME, CASE)

    fun parseDefinitions(): List<ASTDefinition> {
        val result = ArrayList<ASTDefinition>()

        while (lexer.hasMoreTokens())
            result.add(parseDefinition())

        return result
    }

    fun parseDefinition(): ASTDefinition =
        if (lexer.nextTokenIs(TokenType.DATA))
            dataTypeParser.parseDataDefinition()
        else
            parseValueDefinition()

    private fun parseValueDefinition(): ASTValueDefinition = when {
        isSimpleDefinition() ->
            parseSimpleDefinition()
        isOperatorDefinition() ->
            parseOperatorDefinition()
        else ->
            parseNormalValueDefinition()
    }

    private fun isSimpleDefinition(): Boolean =
        lexer.withSavedState {
            try {
                parseIdentifier()
                lexer.nextTokenIs(TokenType.ASSIGN)
            } catch (e: SyntaxException) {
                false
            }
        }

    private fun isOperatorDefinition(): Boolean =
        lexer.withSavedState {
            try {
                patternParser.parseSimplePattern()
                lexer.nextTokenIs(TokenType.OPERATOR)
            } catch (e: SyntaxException) {
                false
            }
        }

    private fun parseSimpleDefinition(): ASTValueDefinition {
        lexer.pushBlockStartAtNextToken()

        val name = parseIdentifier()

        lexer.expectToken(TokenType.ASSIGN)
        val value = parseExpression()
        lexer.expectToken(TokenType.END)
        return AST.define(name, value)
    }

    // <pattern> <op> <pattern> = <exp> ;;
    private fun parseOperatorDefinition(): ASTValueDefinition {
        lexer.pushBlockStartAtNextToken()

        val left = patternParser.parseSimplePattern()
        val op = lexer.readTokenValue(TokenType.OPERATOR)
        val right = patternParser.parseSimplePattern()

        lexer.expectToken(TokenType.ASSIGN)

        val value = parseExpression()
        lexer.expectToken(TokenType.END)

        val functionBuilder = FunctionBuilder()
        functionBuilder.addAlternative(listOf(left, right), value)
        return AST.define(op.toSymbol(), functionBuilder.build())
    }

    // <ident> <pattern>+ = <exp> ;;
    private fun parseNormalValueDefinition(): ASTValueDefinition {
        val functionBuilder = FunctionBuilder()

        var name: Symbol? = null
        while (name == null || nextTokenIsIdentifier(name)) {
            lexer.pushBlockStartAtNextToken()
            name = parseIdentifier()

            val args = ArrayList<Pattern>()

            while (!lexer.nextTokenIs(TokenType.ASSIGN))
                args.add(patternParser.parseSimplePattern())

            lexer.expectToken(TokenType.ASSIGN)

            val value = parseExpression()
            lexer.expectToken(TokenType.END)
            functionBuilder.addAlternative(args, value)
        }

        return AST.define(name, functionBuilder.build())
    }

    private fun nextTokenIsIdentifier(name: Symbol) =
        lexer.nextTokenIs(TokenType.IDENTIFIER) && lexer.peekTokenValue(TokenType.IDENTIFIER) == name.toString()

    fun parseExpression(): ASTExpression {
        var exp = parseExp(0)

        while (true) {
            if (lexer.readMatchingToken(TokenType.SEMICOLON)) {
                val rhs = parseExp(0)
                exp = AST.sequence(exp, rhs)
            } else {
                return exp
            }
        }
    }

    private fun parseExp(level: Int): ASTExpression =
        if (level <= operators.maxLevel)
            parseExpN(level)
        else
            parseApplicative()

    private fun parseExpN(level: Int): ASTExpression {
        var exp = parseExp(level + 1)

        while (true) {
            val op = lexer.readOperatorMatchingLevel(level) ?: return exp

            val rhs = parseExp(if (op.associativity == Associativity.LEFT) level + 1 else level)
            exp = binary(op, exp, rhs)
        }
    }

    private fun parseApplicative(): ASTExpression {
        var exp = parsePrimitive()

        while (lexer.nextTokenIsOneOf(EXPRESSION_START_TOKENS))
            exp = AST.apply(exp, parsePrimitive())

        return exp
    }

    private fun parsePrimitive(): ASTExpression =
        when (lexer.peekTokenType()) {
            EOF         -> parseError("unexpected eof")
            IF          -> parseIf()
            LET         -> parseLet()
            LAMBDA      -> parseLambda()
            CASE        -> parseCase()
            LPAREN      -> parseParens()
            LBRACKET    -> parseList()
            LITERAL     -> AST.constant(lexer.readTokenValue(LITERAL))
            else        -> parseVariableOrConstructor()
        }

    // if <expr> then <expr> else <expr>
    private fun parseIf(): ASTExpression {
        lexer.expectToken(IF)
        val test = parseExpression()
        lexer.expectToken(TokenType.THEN)
        val cons = parseExpression()
        lexer.expectToken(TokenType.ELSE)
        val alt = parseExpression()

        return AST.ifExp(test, cons, alt)
    }

    // case <exp> of <alternative>+
    private fun parseCase(): ASTExpression {
        lexer.expectIndentStartToken(CASE)
        val exp = parseExpression()
        lexer.expectToken(TokenType.OF)

        val alts = ArrayList<ASTAlternative>()
        do {
            alts.add(parseAlternative())
        } while (!lexer.nextTokenIs(TokenType.END))
        lexer.expectToken(TokenType.END)

        return AST.caseExp(exp, alts)
    }

    private fun parseAlternative(): ASTAlternative {
        val pattern = patternParser.parsePattern()
        lexer.expectIndentStartToken(TokenType.RIGHT_ARROW)
        val exp = parseExpression()
        lexer.expectToken(TokenType.END)
        return AST.alternative(pattern, exp)
    }

    // let [rec] <ident> <ident>* = <expr> in <expr>
    private fun parseLet(): ASTExpression {
        lexer.expectToken(LET)
        val recursive = lexer.readMatchingToken(TokenType.REC)

        var name = parseIdentifier()

        val args = ArrayList<Symbol>()

        if (lexer.nextTokenIs(TokenType.OPERATOR)) {
            val op = lexer.readTokenValue(TokenType.OPERATOR)
            args.add(name)
            args.add(parseIdentifier())
            name = op.toSymbol()

            lexer.expectToken(TokenType.ASSIGN)
        } else {
            while (!lexer.readMatchingToken(TokenType.ASSIGN))
                args.add(parseIdentifier())
        }

        var value = parseExpression()
        lexer.expectToken(TokenType.IN)
        val body = parseExpression()

        if (args.any())
            value = AST.lambda(args, value)

        return AST.let(recursive, ImplicitBinding(name, value), body)
    }

    // \ <ident> -> expr
    private fun parseLambda(): ASTExpression {
        lexer.expectToken(LAMBDA)

        val args = ArrayList<Pattern>()

        do {
            args.add(patternParser.parseSimplePattern())
        } while (!lexer.readMatchingToken(TokenType.RIGHT_ARROW))

        val builder = FunctionBuilder()
        builder.addAlternative(args, parseExpression())
        return builder.build()
    }

    // () | (<op>) | ( <expr> )
    private fun parseParens(): ASTExpression {
        lexer.expectToken(LPAREN)
        if (lexer.readMatchingToken(TokenType.RPAREN))
            return AST.constructor(ConstructorNames.UNIT)

        if (lexer.nextTokenIs(TokenType.OPERATOR)) {
            val op = lexer.readTokenValue(TokenType.OPERATOR)
            lexer.expectToken(TokenType.RPAREN)

            return operatorExp(op)
        }

        val exps = ArrayList<ASTExpression>()

        do {
            exps.add(parseExpression())
        } while (lexer.readMatchingToken(TokenType.COMMA))

        lexer.expectToken(TokenType.RPAREN)

        if (exps.size == 1)
            return exps.first()
        else
            return AST.tuple(exps)
    }

    // []
    // [<exp> (,<exp>)*]
    private fun parseList(): ASTExpression {
        val exps = ArrayList<ASTExpression>()

        lexer.expectToken(LBRACKET)

        if (!lexer.nextTokenIs(TokenType.RBRACKET)) {
            do {
                exps.add(parseExpression())
            } while (lexer.readMatchingToken(TokenType.COMMA))
        }

        lexer.expectToken(TokenType.RBRACKET)

        return AST.list(exps)
    }

    private fun parseVariableOrConstructor(): ASTExpression {
        if (lexer.nextTokenIs(TokenType.IDENTIFIER))
            return AST.variable(lexer.readTokenValue(TokenType.IDENTIFIER))

        if (lexer.nextTokenIs(TokenType.TYPE_OR_CTOR_NAME))
            return AST.constructor(lexer.readTokenValue(TokenType.TYPE_OR_CTOR_NAME))

        if (lexer.readMatchingToken(LPAREN)) {
            val op = lexer.readTokenValue(TokenType.OPERATOR)
            lexer.expectToken(TokenType.RPAREN)

            return operatorExp(op)
        }

        lexer.expectFailure("identifier or type constructor")
    }

    private fun parseIdentifier(): Symbol {
        if (lexer.nextTokenIs(TokenType.IDENTIFIER))
            return Symbol(lexer.readTokenValue(TokenType.IDENTIFIER))

        if (lexer.readMatchingToken(LPAREN)) {
            val op = lexer.readTokenValue(TokenType.OPERATOR)
            lexer.expectToken(TokenType.RPAREN)
            return op.toSymbol()
        }

        lexer.expectFailure("identifier")
    }

    private fun parseError(s: String): Nothing =
        lexer.parseError(s)

    private fun binary(op: Operator, lhs: ASTExpression, rhs: ASTExpression): ASTExpression {
        return AST.apply(operatorExp(op), lhs, rhs)
    }

    private fun operatorExp(op: Operator): ASTExpression =
        if (op.isConstructor) AST.constructor(op.toString()) else AST.variable(op.toSymbol())
}
