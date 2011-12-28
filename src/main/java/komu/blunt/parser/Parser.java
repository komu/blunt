package komu.blunt.parser;

import static java.util.Arrays.asList;
import static komu.blunt.objects.Symbol.symbol;
import static komu.blunt.parser.Associativity.LEFT;
import static komu.blunt.parser.TokenType.ASSIGN;
import static komu.blunt.parser.TokenType.CONSTRUCTOR_NAME;
import static komu.blunt.parser.TokenType.IDENTIFIER;
import static komu.blunt.parser.TokenType.IF;
import static komu.blunt.parser.TokenType.LAMBDA;
import static komu.blunt.parser.TokenType.LBRACKET;
import static komu.blunt.parser.TokenType.LET;
import static komu.blunt.parser.TokenType.LITERAL;
import static komu.blunt.parser.TokenType.LPAREN;
import static komu.blunt.parser.TokenType.OPERATOR;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import komu.blunt.ast.ASTApplication;
import komu.blunt.ast.ASTConstant;
import komu.blunt.ast.ASTConstructor;
import komu.blunt.ast.ASTDefine;
import komu.blunt.ast.ASTExpression;
import komu.blunt.ast.ASTIf;
import komu.blunt.ast.ASTLambda;
import komu.blunt.ast.ASTLet;
import komu.blunt.ast.ASTLetRec;
import komu.blunt.ast.ASTList;
import komu.blunt.ast.ASTSequence;
import komu.blunt.ast.ASTTuple;
import komu.blunt.ast.ASTVariable;
import komu.blunt.ast.ImplicitBinding;
import komu.blunt.objects.Symbol;
import komu.blunt.objects.Unit;

public final class Parser {

    private final Lexer lexer;
    private final OperatorSet operators = new OperatorSet();

    @SuppressWarnings("unchecked")
    private static final List<TokenType<?>> expressionStartTokens =
        Arrays.asList(IF, LET, LAMBDA, LPAREN, LBRACKET, LITERAL, IDENTIFIER, CONSTRUCTOR_NAME);

    public Parser(String source) {
        this.lexer = new Lexer(source);
    }

    public static ASTExpression parseExpression(String source) {
        return new Parser(source).parseExpression();
    }
    
    public List<ASTDefine> parseDefinitions() {
        List<ASTDefine> result = new ArrayList<ASTDefine>();
        
        while (lexer.peekTokenType() != TokenType.EOF)
            result.add(parseDefinition());
        
        return result;
    }
    
    // <ident> <op> <ident> = <exp> ;;
    // <ident> <ident>* = <exp> ;;
    private ASTDefine parseDefinition() {
        Symbol name = parseIdentifier();
        List<Symbol> args = new ArrayList<Symbol>();
        
        if (lexer.nextTokenIs(OPERATOR)) {
            Operator op = lexer.readToken(OPERATOR).value;
            args.add(name);
            args.add(parseIdentifier());
            name = symbol(op.toString());

            expectToken(ASSIGN);
        } else {
            while (!lexer.readMatchingToken(ASSIGN))
                args.add(parseIdentifier());
        }

        lexer.pushIndentLevelAtNextToken();
        
        ASTExpression value = parseExpression();
        expectToken(TokenType.END);

        if (args.isEmpty())
            return new ASTDefine(name, value);
        else
            return new ASTDefine(name, new ASTLambda(args, value));
    }

    public ASTExpression parseExpression() {
        ASTExpression exp = parseExp(0);

        while (true) {
            if (customOperator(lexer.peekToken())) {
                Operator operator = lexer.readToken(OPERATOR).value;
                Associativity associativity = operators.getAssociativity(operator);
                ASTExpression rhs = associativity == LEFT ? parseExp(0) : parseExpression();
                exp = binary(operator, exp, rhs);
            } else if (lexer.readMatchingToken(TokenType.SEMICOLON)) {
                ASTExpression rhs = parseExp(0);
                exp = new ASTSequence(exp, rhs);
            } else {
                return exp;
            }
        }
    }

    private static boolean customOperator(Token<?> token) {
        return token.type == OPERATOR && !token.asType(OPERATOR).value.isBuiltin();
    }

    private ASTExpression parseExp(int level) {
        if (level <= operators.getMaxLevel())
            return parseExpN(level);
        else
            return parseApplicative();
    }

    private ASTExpression parseExpN(int level) {
        ASTExpression exp = parseExp(level+1);

        while (true) {
            Operator op = lexer.readMatching(OPERATOR, operators.operator(level));
            if (op != null) {
                Associativity associativity = operators.getAssociativity(op);
                ASTExpression rhs = parseExp(associativity == LEFT ? level+1 : level);
                exp = binary(op, exp, rhs);
            } else {
                return exp;
            }
        }
    }

    private ASTExpression parseApplicative() {
        ASTExpression exp = parsePrimitive();

        while (expressionStartTokens.contains(lexer.peekTokenType()))
            exp = new ASTApplication(exp, parsePrimitive());

        return exp;
    }

    private ASTExpression parsePrimitive() {
        TokenType type = lexer.peekTokenType();
        
        if (type == TokenType.EOF)
            throw parseError("unexpected eof");
        
        if (type == TokenType.IF)
            return parseIf();
        
        if (type == TokenType.LET)
            return parseLet();
            
        if (type == TokenType.LAMBDA)
            return parseLambda();
        
        if (type == TokenType.LPAREN)
            return parseParens();
        
        if (type == TokenType.LBRACKET)
            return parseList();

        if (type == TokenType.LITERAL)
            return new ASTConstant(lexer.readToken(TokenType.LITERAL).value);

        return parseVariableOrConstructor();
    }

    // if <expr> then <expr> else <expr>
    private ASTExpression parseIf() {
        expectToken(TokenType.IF);
        ASTExpression test = parseExpression();
        expectToken(TokenType.THEN);
        ASTExpression cons = parseExpression();
        expectToken(TokenType.ELSE);
        ASTExpression alt = parseExpression();

        return new ASTIf(test, cons, alt);
    }

    // let [rec] <ident> <ident>* = <expr> in <expr>
    private ASTExpression parseLet() {
        expectToken(TokenType.LET);
        boolean recursive = lexer.readMatchingToken(TokenType.REC);
        
        Symbol name = parseIdentifier();

        List<Symbol> args = new ArrayList<Symbol>();

        if (lexer.nextTokenIs(OPERATOR)) {
            Operator op = lexer.readToken(OPERATOR).value;
            args.add(name);
            args.add(parseIdentifier());
            name = symbol(op.toString());

            expectToken(ASSIGN);
        } else {
            while (!lexer.readMatchingToken(ASSIGN))
                args.add(parseIdentifier());
        }

        ASTExpression value = parseExpression();
        expectToken(TokenType.IN);
        ASTExpression body = parseExpression();

        if (!args.isEmpty())
            value = new ASTLambda(args, value);

        List<ImplicitBinding> bindings = asList(new ImplicitBinding(name, value));
        if (recursive)
            return new ASTLetRec(bindings, body);
        else
            return new ASTLet(bindings, body);
    }

    // \ <ident> -> expr
    private ASTExpression parseLambda() {
        expectToken(TokenType.LAMBDA);
        
        List<Symbol> args = new ArrayList<Symbol>();
        
        do {
            args.add(parseIdentifier());
        } while (!lexer.readMatchingToken(OPERATOR, Operator.RIGHT_ARROW));

        return new ASTLambda(args, parseExpression());
    }

    // () | (<op>) | ( <expr> )
    private ASTExpression parseParens() {
        expectToken(TokenType.LPAREN);
        if (lexer.readMatchingToken(TokenType.RPAREN))
            return new ASTConstant(Unit.INSTANCE);
        
        if (lexer.nextTokenIs(OPERATOR)) {
            Operator op = lexer.readToken(OPERATOR).value;
            expectToken(TokenType.RPAREN);
            
            return op.isConstructor() ? new ASTConstructor(op.toString()) : new ASTVariable(op.toString());
        }
        
        List<ASTExpression> exps = new ArrayList<ASTExpression>();
        
        do {
            exps.add(parseExpression());    
        } while (lexer.readMatchingToken(TokenType.COMMA));

        expectToken(TokenType.RPAREN);
        
        if (exps.size() == 1)
            return exps.get(0);
        else
            return new ASTTuple(exps);
    }
    
    // []
    // [<exp> (,<exp>)*]
    private ASTExpression parseList() {
        ASTList list = new ASTList();

        expectToken(TokenType.LBRACKET);
        
        if (lexer.peekTokenType() != TokenType.RBRACKET) {
            do {
                list.add(parseExpression());
            } while (lexer.readMatchingToken(TokenType.COMMA));
        }

        expectToken(TokenType.RBRACKET);

        return list;
    }

    private ASTExpression parseVariableOrConstructor() {
        Token<?> token = lexer.readToken();

        if (token.type == IDENTIFIER)
            return new ASTVariable(token.asType(IDENTIFIER).value);

        if (token.type == CONSTRUCTOR_NAME)
            return new ASTConstructor(token.asType(CONSTRUCTOR_NAME).value);

        if (token.type == TokenType.LPAREN) {
            Operator op = lexer.readToken(OPERATOR).value;
            expectToken(TokenType.RPAREN);
            
            if (op.isConstructor())
                return new ASTConstructor(op.toString());
            else
                return new ASTVariable(op.toSymbol());
        }

        throw parseError("expected identifier or type constructor, got " + token);
    }

    private Symbol parseIdentifier() {
        Token<?> token = lexer.readToken();

        if (token.type == IDENTIFIER)
            return symbol(token.asType(IDENTIFIER).value);

        if (token.type == TokenType.LPAREN) {
            Token<Operator> op = lexer.readToken(OPERATOR);
            expectToken(TokenType.RPAREN);
            return op.asType(OPERATOR).value.toSymbol();
        }

        throw parseError("expected identifier, got " + token);
    }

    private void expectToken(TokenType<?> expected) {
        Token<?> token = lexer.readToken();

        if (expected != token.type)
            throw parseError("expected " + expected + " but got " + token);
    }

    private SyntaxException parseError(final String s) {
        return lexer.parseError(s);
    }

    private static ASTExpression binary(Operator op, ASTExpression lhs, ASTExpression rhs) {
        ASTExpression exp = op.isConstructor() ? new ASTConstructor(op.toString()) : new ASTVariable(op.toString());
        return new ASTApplication(new ASTApplication(exp, lhs), rhs);
    }
}
