package komu.blunt.parser;

import com.google.common.collect.ImmutableList;
import komu.blunt.ast.*;
import komu.blunt.objects.Symbol;
import komu.blunt.types.DataTypeDefinitions;
import komu.blunt.types.patterns.Pattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static komu.blunt.objects.Symbol.symbol;
import static komu.blunt.parser.Associativity.LEFT;
import static komu.blunt.parser.TokenType.*;

public final class Parser {

    private final Lexer lexer;
    private final OperatorSet operators = new OperatorSet();
    private final TypeParser typeParser;
    private final PatternParser patternParser;
    private final DataTypeParser dataTypeParser;

    @SuppressWarnings("unchecked")
    private static final List<TokenType<?>> expressionStartTokens =
        Arrays.asList(IF, LET, LAMBDA, LPAREN, LBRACKET, LITERAL, IDENTIFIER, TYPE_OR_CTOR_NAME, CASE);

    public Parser(String source) {
        this.lexer = new Lexer(source, operators);
        this.typeParser = new TypeParser(lexer);
        this.patternParser = new PatternParser(lexer);
        this.dataTypeParser = new DataTypeParser(lexer, typeParser);
    }

    public static ASTExpression parseExpression(String source) {
        return new Parser(source).parseExpression();
    }
    
    public List<ASTDefinition> parseDefinitions() {
        List<ASTDefinition> result = new ArrayList<ASTDefinition>();
        
        while (lexer.hasMoreTokens())
            result.add(parseDefinition());
        
        return result;
    }
    
    private ASTDefinition parseDefinition() {
        if (lexer.nextTokenIs(DATA))
            return dataTypeParser.parseDataDefinition();
        else
            return parseValueDefinition();
    }


    // <ident> <op> <ident> = <exp> ;;
    // <ident> <ident>* = <exp> ;;
    private ASTValueDefinition parseValueDefinition() {
        lexer.pushBlockStartAtNextToken();

        Symbol name = parseIdentifier();
        List<Symbol> args = new ArrayList<Symbol>();
        
        if (lexer.nextTokenIs(OPERATOR)) {
            Operator op = lexer.readTokenValue(OPERATOR);
            args.add(name);
            args.add(parseIdentifier());
            name = symbol(op.toString());

        } else {
            while (!lexer.nextTokenIs(ASSIGN))
                args.add(parseIdentifier());
        }

        lexer.expectToken(ASSIGN);

        ASTExpression value = parseExpression();
        lexer.expectToken(END);

        if (args.isEmpty())
            return AST.define(name, value);
        else
            return AST.define(name, AST.lambda(args, value));
    }

    public ASTExpression parseExpression() {
        ASTExpression exp = parseExp(0);

        while (true) {
            if (lexer.readMatchingToken(SEMICOLON)) {
                ASTExpression rhs = parseExp(0);
                exp = AST.sequence(exp, rhs);
            } else {
                return exp;
            }
        }
    }

    private ASTExpression parseExp(int level) {
        if (level <= operators.getMaxLevel())
            return parseExpN(level);
        else
            return parseApplicative();
    }

    private ASTExpression parseExpN(int level) {
        ASTExpression exp = parseExp(level + 1);

        while (true) {
            Operator op = lexer.readOperatorMatchingLevel(level);
            if (op != null) {
                ASTExpression rhs = parseExp(op.associativity == LEFT ? level+1 : level);
                exp = binary(op, exp, rhs);
            } else {
                return exp;
            }
        }
    }

    private ASTExpression parseApplicative() {
        ASTExpression exp = parsePrimitive();

        while (lexer.nextTokenIsOneOf(expressionStartTokens))
            exp = AST.apply(exp, parsePrimitive());

        return exp;
    }

    private ASTExpression parsePrimitive() {
        TokenType type = lexer.peekTokenType();
        
        if (type == EOF)
            throw parseError("unexpected eof");
        
        if (type == IF)
            return parseIf();
        
        if (type == LET)
            return parseLet();
            
        if (type == LAMBDA)
            return parseLambda();
        
        if (type == CASE)
            return parseCase();
        
        if (type == LPAREN)
            return parseParens();
        
        if (type == LBRACKET)
            return parseList();

        if (type == LITERAL)
            return AST.constant(lexer.readTokenValue(LITERAL));

        return parseVariableOrConstructor();
    }

    // if <expr> then <expr> else <expr>
    private ASTExpression parseIf() {
        lexer.expectToken(IF);
        ASTExpression test = parseExpression();
        lexer.expectToken(THEN);
        ASTExpression cons = parseExpression();
        lexer.expectToken(ELSE);
        ASTExpression alt = parseExpression();

        return AST.ifExp(test, cons, alt);
    }

    // case <exp> of <alternative>+
    private ASTExpression parseCase() {
        lexer.expectIndentStartToken(CASE);
        ASTExpression exp = parseExpression();
        lexer.expectToken(OF);

        ImmutableList.Builder<ASTAlternative> alts = ImmutableList.builder();
        do {
            alts.add(parseAlternative());
        } while (!lexer.nextTokenIs(END));
        lexer.expectToken(END);

        return AST.caseExp(exp, alts.build());
    }

    private ASTAlternative parseAlternative() {
        Pattern pattern = patternParser.parsePattern();
        lexer.expectIndentStartToken(RIGHT_ARROW);
        ASTExpression exp = parseExpression();
        lexer.expectToken(END);
        return AST.alternative(pattern, exp);
    }

    // let [rec] <ident> <ident>* = <expr> in <expr>
    private ASTExpression parseLet() {
        lexer.expectToken(LET);
        boolean recursive = lexer.readMatchingToken(REC);
        
        Symbol name = parseIdentifier();

        List<Symbol> args = new ArrayList<Symbol>();

        if (lexer.nextTokenIs(OPERATOR)) {
            Operator op = lexer.readTokenValue(OPERATOR);
            args.add(name);
            args.add(parseIdentifier());
            name = symbol(op.toString());

            lexer.expectToken(ASSIGN);
        } else {
            while (!lexer.readMatchingToken(ASSIGN))
                args.add(parseIdentifier());
        }

        ASTExpression value = parseExpression();
        lexer.expectToken(IN);
        ASTExpression body = parseExpression();

        if (!args.isEmpty())
            value = AST.lambda(args, value);

        return AST.let(recursive, new ImplicitBinding(name, value), body);
    }

    // \ <ident> -> expr
    private ASTExpression parseLambda() {
        lexer.expectToken(LAMBDA);

        List<Symbol> args = new ArrayList<Symbol>();
        
        do {
            args.add(parseIdentifier());
        } while (!lexer.readMatchingToken(RIGHT_ARROW));

        return AST.lambda(args, parseExpression());
    }

    // () | (<op>) | ( <expr> )
    private ASTExpression parseParens() {
        lexer.expectToken(LPAREN);
        if (lexer.readMatchingToken(RPAREN))
            return AST.constructor(DataTypeDefinitions.UNIT);
        
        if (lexer.nextTokenIs(OPERATOR)) {
            Operator op = lexer.readTokenValue(OPERATOR);
            lexer.expectToken(RPAREN);

            return op.isConstructor() ? AST.constructor(op.toString()) : AST.variable(op.toString());
        }

        List<ASTExpression> exps = new ArrayList<ASTExpression>();
        
        do {
            exps.add(parseExpression());    
        } while (lexer.readMatchingToken(COMMA));

        lexer.expectToken(RPAREN);

        if (exps.size() == 1)
            return exps.get(0);
        else
            return AST.tuple(exps);
    }
    
    // []
    // [<exp> (,<exp>)*]
    private ASTExpression parseList() {
        AST.ListBuilder list = AST.listBuilder();

        lexer.expectToken(LBRACKET);

        if (!lexer.nextTokenIs(RBRACKET)) {
            do {
                list.add(parseExpression());
            } while (lexer.readMatchingToken(COMMA));
        }

        lexer.expectToken(RBRACKET);

        return list.build();
    }

    private ASTExpression parseVariableOrConstructor() {
        if (lexer.nextTokenIs(IDENTIFIER))
            return AST.variable(lexer.readTokenValue(IDENTIFIER));

        if (lexer.nextTokenIs(TYPE_OR_CTOR_NAME))
            return AST.constructor(lexer.readTokenValue(TYPE_OR_CTOR_NAME));

        if (lexer.readMatchingToken(LPAREN)) {
            Operator op = lexer.readTokenValue(OPERATOR);
            lexer.expectToken(RPAREN);

            if (op.isConstructor())
                return AST.constructor(op.toString());
            else
                return AST.variable(op.toString());
        }

        throw lexer.expectFailure("identifier or type constructor");
    }

    private Symbol parseIdentifier() {
        if (lexer.nextTokenIs(IDENTIFIER)) {
            return symbol(lexer.readTokenValue(IDENTIFIER));
        }

        if (lexer.readMatchingToken(LPAREN)) {
            Operator op = lexer.readTokenValue(OPERATOR);
            lexer.expectToken(RPAREN);
            return op.toSymbol();
        }

        throw lexer.expectFailure("identifier");
    }

    private SyntaxException parseError(final String s) {
        return lexer.parseError(s);
    }

    private static ASTExpression binary(Operator op, ASTExpression lhs, ASTExpression rhs) {
        ASTExpression exp = op.isConstructor() ? AST.constructor(op.toString()) : AST.variable(op.toString());
        
        return AST.apply(exp, lhs, rhs);
    }
}
