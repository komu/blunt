package komu.blunt.parser

import komu.blunt.ast.*
import komu.blunt.objects.Symbol
import komu.blunt.parser.TokenType.Companion.ASSIGN
import komu.blunt.parser.TokenType.Companion.CASE
import komu.blunt.parser.TokenType.Companion.DATA
import komu.blunt.parser.TokenType.Companion.ELSE
import komu.blunt.parser.TokenType.Companion.END
import komu.blunt.parser.TokenType.Companion.EOF
import komu.blunt.parser.TokenType.Companion.IDENTIFIER
import komu.blunt.parser.TokenType.Companion.IF
import komu.blunt.parser.TokenType.Companion.LAMBDA
import komu.blunt.parser.TokenType.Companion.LBRACKET
import komu.blunt.parser.TokenType.Companion.LET
import komu.blunt.parser.TokenType.Companion.LITERAL
import komu.blunt.parser.TokenType.Companion.LPAREN
import komu.blunt.parser.TokenType.Companion.OF
import komu.blunt.parser.TokenType.Companion.OPERATOR
import komu.blunt.parser.TokenType.Companion.RBRACKET
import komu.blunt.parser.TokenType.Companion.REC
import komu.blunt.parser.TokenType.Companion.RIGHT_ARROW
import komu.blunt.parser.TokenType.Companion.RPAREN
import komu.blunt.parser.TokenType.Companion.SEMICOLON
import komu.blunt.parser.TokenType.Companion.THEN
import komu.blunt.parser.TokenType.Companion.TYPE_OR_CTOR_NAME
import komu.blunt.types.ConstructorNames

fun parseExpression(source: String) =
    Parser(source).parseExpression()

class Parser(source: String) {

    private val operators = OperatorSet()
    private val lexer = Lexer(source, operators)
    private val typeParser = TypeParser(lexer)
    private val patternParser = PatternParser(lexer)
    private val dataTypeParser = DataTypeParser(lexer, typeParser)

    private val EXPRESSION_START_TOKENS =
        listOf(IF, LET, LAMBDA, LPAREN, LBRACKET, LITERAL, IDENTIFIER, TYPE_OR_CTOR_NAME, CASE)

    fun parseDefinitions(): List<ASTDefinition> =
        lexer.listOf {
            if (lexer.hasMoreTokens())
                parseDefinition()
            else
                null
        }

    fun parseDefinition(): ASTDefinition =
        if (lexer.nextTokenIs(DATA))
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
                lexer.nextTokenIs(ASSIGN)
            } catch (e: SyntaxException) {
                false
            }
        }

    private fun isOperatorDefinition(): Boolean =
        lexer.withSavedState {
            try {
                patternParser.parseSimplePattern()
                lexer.nextTokenIs(OPERATOR)
            } catch (e: SyntaxException) {
                false
            }
        }

    private fun parseSimpleDefinition(): ASTValueDefinition {
        lexer.pushBlockStartAtNextToken()

        val name = parseIdentifier()

        lexer.expectToken(ASSIGN)
        val value = parseExpression()
        lexer.expectToken(END)
        return ASTValueDefinition(name, value)
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

        return ASTValueDefinition(op.toSymbol(), buildFunction(listOf(left, right), value))
    }

    // <ident> <pattern>+ = <exp> ;;
    private fun parseNormalValueDefinition(): ASTValueDefinition {
        val functionBuilder = FunctionBuilder()

        var name: Symbol? = null
        while (name == null || nextTokenIsIdentifier(name)) {
            lexer.pushBlockStartAtNextToken()
            name = parseIdentifier()

            val args = lexer.listOf1 {
                if (lexer.nextTokenIs(ASSIGN))
                    null
                else
                    patternParser.parseSimplePattern()
            }

            lexer.expectToken(ASSIGN)

            val value = parseExpression()
            lexer.expectToken(END)
            functionBuilder.addAlternative(args, value)
        }

        return ASTValueDefinition(name, functionBuilder.build())
    }

    private fun nextTokenIsIdentifier(name: Symbol) =
        lexer.nextTokenIs(IDENTIFIER) && lexer.peekTokenValue(IDENTIFIER) == name.toString()

    fun parseExpression(): ASTExpression {
        var exp = parseExp(0)

        while (true) {
            if (lexer.readMatchingToken(SEMICOLON)) {
                val rhs = parseExp(0)
                exp = ASTExpression.Sequence(exp, rhs)
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
            LITERAL     -> ASTExpression.Constant(lexer.readTokenValue(LITERAL))
            else        -> parseVariableOrConstructor()
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

        val alts = lexer.listOf1 {
            if (lexer.nextTokenIs(END))
                null
            else
                parseAlternative()
        }
        lexer.expectToken(END)

        return ASTExpression.Case(exp, alts)
    }

    private fun parseAlternative(): ASTAlternative {
        val pattern = patternParser.parsePattern()
        lexer.expectIndentStartToken(RIGHT_ARROW)
        val exp = parseExpression()
        lexer.expectToken(END)
        return ASTAlternative(pattern, exp)
    }

    // let [rec] <ident> <ident>* = <expr> in <expr>
    private fun parseLet(): ASTExpression {
        lexer.expectToken(LET)
        val recursive = lexer.readMatchingToken(REC)

        var name = parseIdentifier()

        val args = if (lexer.nextTokenIs(OPERATOR)) {
            name = lexer.readTokenValue(OPERATOR).toSymbol()
            val ident = parseIdentifier()

            lexer.expectToken(ASSIGN)
            listOf(name, ident)
        } else {
            lexer.listOf {
                if (lexer.readMatchingToken(ASSIGN))
                    null
                else
                    parseIdentifier()
            }
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

        val args = lexer.listOf1 {
            if (lexer.readMatchingToken(RIGHT_ARROW))
                null
            else
                patternParser.parseSimplePattern()
        }

        return buildFunction(args, parseExpression())
    }

    // () | (<op>) | ( <expr> )
    private fun parseParens(): ASTExpression =
        lexer.inParens {
            when {
                lexer.nextTokenIs(RPAREN) ->
                    AST.constructor(ConstructorNames.UNIT)

                lexer.nextTokenIs(OPERATOR) ->
                    operatorExp(lexer.readTokenValue(OPERATOR))

                else -> {
                    val exps = lexer.commaSep { parseExpression() }
                    exps.singleOrNull() ?: ASTExpression.tuple(exps)
                }
            }
        }

    // []
    // [<exp> (,<exp>)*]
    private fun parseList(): ASTExpression =
        lexer.inBrackets {
            if (!lexer.nextTokenIs(RBRACKET))
                ASTExpression.list(lexer.commaSep { parseExpression() })
            else
                ASTExpression.list(emptyList())
        }

    private fun parseVariableOrConstructor(): ASTExpression = when {
        lexer.nextTokenIs(IDENTIFIER) ->
            ASTExpression.Variable(lexer.readTokenValue(IDENTIFIER))

        lexer.nextTokenIs(TYPE_OR_CTOR_NAME) ->
            AST.constructor(lexer.readTokenValue(TYPE_OR_CTOR_NAME))

        lexer.nextTokenIs(LPAREN) ->
            lexer.inParens { operatorExp(lexer.readTokenValue(OPERATOR)) }

        else -> lexer.expectFailure("identifier or type constructor")
    }

    private fun parseIdentifier(): Symbol = when {
        lexer.nextTokenIs(IDENTIFIER) ->
            Symbol(lexer.readTokenValue(IDENTIFIER))
        lexer.nextTokenIs(LPAREN) ->
            lexer.inParens { lexer.readTokenValue(OPERATOR).toSymbol() }
        else ->
            lexer.expectFailure("identifier")
    }

    private fun parseError(s: String): Nothing =
        lexer.parseError(s)

    private fun binary(op: Operator, lhs: ASTExpression, rhs: ASTExpression) =
        AST.apply(operatorExp(op), lhs, rhs)

    private fun operatorExp(op: Operator): ASTExpression =
        if (op.isConstructor) AST.constructor(op.toString()) else ASTExpression.Variable(op.toSymbol())
}
