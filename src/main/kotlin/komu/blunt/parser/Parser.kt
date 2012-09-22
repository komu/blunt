package komu.blunt.parser

import java.util.ArrayList
import komu.blunt.ast.*
import komu.blunt.objects.Symbol
import komu.blunt.parser.TokenType.*
import komu.blunt.types.ConstructorNames
import komu.blunt.types.patterns.Pattern

class Parser(source: String) {

    private val operators = OperatorSet()
    private val lexer = Lexer(source, operators)
    private val typeParser = TypeParser(lexer)
    private val patternParser = PatternParser(lexer)
    private val dataTypeParser = DataTypeParser(lexer, typeParser)

    private val EXPRESSION_START_TOKENS =
        arrayList(TokenType.IF, TokenType.LET, TokenType.LAMBDA, TokenType.LPAREN, TokenType.LBRACKET, TokenType.LITERAL, TokenType.IDENTIFIER, TokenType.TYPE_OR_CTOR_NAME, TokenType.CASE)

    class object {
        fun parseExpression(source: String) =
            Parser(source).parseExpression()
    }

    fun parseDefinitions(): List<ASTDefinition> {
        val result = listBuilder<ASTDefinition>()

        while (lexer.hasMoreTokens())
            result.add(parseDefinition())

        return result.build()
    }

    fun parseDefinition(): ASTDefinition =
        if (lexer.nextTokenIs(TokenType.DATA))
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
        functionBuilder.addAlternative(arrayList(left, right), value)
        return AST.define(op.toSymbol(), functionBuilder.build())
    }

    // <ident> <pattern>+ = <exp> ;;
    private fun parseNormalValueDefinition(): ASTValueDefinition {
        val functionBuilder = FunctionBuilder()

        var name: Symbol? = null
        while (name == null || nextTokenIsIdentifier(name!!)) {
            lexer.pushBlockStartAtNextToken()
            name = parseIdentifier()

            val args = listBuilder<Pattern>()

            while (!lexer.nextTokenIs(TokenType.ASSIGN))
                args.add(patternParser.parseSimplePattern())

            lexer.expectToken(TokenType.ASSIGN)

            val value = parseExpression()
            lexer.expectToken(TokenType.END)
            functionBuilder.addAlternative(args.build(), value)
        }

        return AST.define(name!!, functionBuilder.build())
    }

    private fun nextTokenIsIdentifier(name: Symbol) =
        lexer.nextTokenIs(TokenType.IDENTIFIER) && lexer.peekTokenValue(TokenType.IDENTIFIER) == name.toString()

    public fun parseExpression(): ASTExpression {
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
            val op = lexer.readOperatorMatchingLevel(level)
            if (op != null) {
                val rhs = parseExp(if (op.associativity == Associativity.LEFT) level+1 else level)
                exp = binary(op, exp, rhs)
            } else {
                return exp
            }
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
            TokenType.EOF      -> throw parseError("unexpected eof")
            TokenType.IF       -> parseIf()
            TokenType.LET      -> parseLet()
            TokenType.LAMBDA   -> parseLambda()
            TokenType.CASE     -> parseCase()
            TokenType.LPAREN   -> parseParens()
            TokenType.LBRACKET -> parseList()
            TokenType.LITERAL  -> AST.constant(lexer.readTokenValue(TokenType.LITERAL))
            else     -> parseVariableOrConstructor()
        }

    // if <expr> then <expr> else <expr>
    private fun parseIf(): ASTExpression {
        lexer.expectToken(TokenType.IF)
        val test = parseExpression()
        lexer.expectToken(TokenType.THEN)
        val cons = parseExpression()
        lexer.expectToken(TokenType.ELSE)
        val alt = parseExpression()

        return AST.ifExp(test, cons, alt)
    }

    // case <exp> of <alternative>+
    private fun parseCase(): ASTExpression {
        lexer.expectIndentStartToken(TokenType.CASE)
        val exp = parseExpression()
        lexer.expectToken(TokenType.OF)

        val alts = listBuilder<ASTAlternative>()
        do {
            alts.add(parseAlternative())
        } while (!lexer.nextTokenIs(TokenType.END))
        lexer.expectToken(TokenType.END)

        return AST.caseExp(exp, alts.build())
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
        lexer.expectToken(TokenType.LET)
        val recursive = lexer.readMatchingToken(TokenType.REC)

        var name = parseIdentifier()

        val args = arrayList<Symbol>()

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

        if (!args.isEmpty())
            value = AST.lambda(args, value)

        return AST.let(recursive, ImplicitBinding(name, value), body)
    }

    // \ <ident> -> expr
    private fun parseLambda(): ASTExpression {
        lexer.expectToken(TokenType.LAMBDA)

        val args = listBuilder<Pattern>()

        do {
            args.add(patternParser.parseSimplePattern())
        } while (!lexer.readMatchingToken(TokenType.RIGHT_ARROW))

        val builder = FunctionBuilder()
        builder.addAlternative(args.build(), parseExpression())
        return builder.build()
    }

    // () | (<op>) | ( <expr> )
    private fun parseParens(): ASTExpression {
        lexer.expectToken(TokenType.LPAREN)
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
        val list = AST.bluntListBuilder()

        lexer.expectToken(TokenType.LBRACKET)

        if (!lexer.nextTokenIs(TokenType.RBRACKET)) {
            do {
                list.add(parseExpression())
            } while (lexer.readMatchingToken(TokenType.COMMA))
        }

        lexer.expectToken(TokenType.RBRACKET)

        return list.build()
    }

    private fun parseVariableOrConstructor(): ASTExpression {
        if (lexer.nextTokenIs(TokenType.IDENTIFIER))
            return AST.variable(lexer.readTokenValue(TokenType.IDENTIFIER))

        if (lexer.nextTokenIs(TokenType.TYPE_OR_CTOR_NAME))
            return AST.constructor(lexer.readTokenValue(TokenType.TYPE_OR_CTOR_NAME))

        if (lexer.readMatchingToken(TokenType.LPAREN)) {
            val op = lexer.readTokenValue(TokenType.OPERATOR)
            lexer.expectToken(TokenType.RPAREN)

            return operatorExp(op)
        }

        throw lexer.expectFailure("identifier or type constructor")
    }

    private fun parseIdentifier(): Symbol {
        if (lexer.nextTokenIs(TokenType.IDENTIFIER))
            return Symbol(lexer.readTokenValue(TokenType.IDENTIFIER))

        if (lexer.readMatchingToken(TokenType.LPAREN)) {
            val op = lexer.readTokenValue(TokenType.OPERATOR)
            lexer.expectToken(TokenType.RPAREN)
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
