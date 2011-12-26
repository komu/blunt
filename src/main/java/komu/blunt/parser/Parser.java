package komu.blunt.parser;

import komu.blunt.ast.*;
import komu.blunt.eval.SyntaxException;
import komu.blunt.objects.Symbol;
import komu.blunt.objects.Unit;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static komu.blunt.objects.Symbol.symbol;
import static komu.blunt.parser.Associativity.LEFT;
import static komu.blunt.parser.TokenType.ASSIGN;
import static komu.blunt.parser.TokenType.OPERATOR;

public final class Parser {

    private final Lexer lexer;
    private final OperatorSet operators = new OperatorSet();
    private static final List<TokenType> expressionStartTokens =
        asList(TokenType.IF, TokenType.LET, TokenType.LAMBDA, TokenType.LPAREN, TokenType.LBRACKET, TokenType.LITERAL, TokenType.IDENTIFIER);

    public Parser(InputStream in) {
        this(new InputStreamReader(in));
    }

    public Parser(Reader reader) {
        this.lexer = new Lexer(reader);
    }

    public static ASTExpression parse(String input) {
        try {
            return new Parser(new StringReader(input)).parseExpression();
        } catch (IOException e) {
            throw new RuntimeException("unexpected IOException: " + e, e);
        }
    }
    
    public List<ASTDefine> parseDefinitions() throws IOException {
        List<ASTDefine> result = new ArrayList<ASTDefine>();
        
        while (lexer.peekToken().type != TokenType.EOF)
            result.add(parseDefinition());
        
        return result;
    }

    // <ident> <op> <ident> = <exp> ;;
    // <ident> <ident>* = <exp> ;;
    private ASTDefine parseDefinition() throws IOException {
        Symbol name = parseIdentifier();
        List<Symbol> args = new ArrayList<Symbol>();
        
        if (lexer.peekToken().type == OPERATOR) {
            Operator op = lexer.readToken().value(Operator.class);
            args.add(name);
            args.add(parseIdentifier());
            name = symbol(op.toString());

            expectToken(ASSIGN);
        } else {
            while (!lexer.readMatchingToken(ASSIGN))
                args.add(parseIdentifier());
        }
        
        ASTExpression value = parseExpression();
        expectToken(TokenType.DOUBLE_SEMI);

        if (args.isEmpty())
            return new ASTDefine(name, value);
        else
            return new ASTDefine(name, new ASTLambda(args, value));
    }

    public ASTExpression parseExpression() throws IOException {
        ASTExpression exp = parseExp(0);

        while (true) {
            if (customOperator(lexer.peekToken())) {
                Operator operator = lexer.readToken().value(Operator.class);
                Associativity associativity = operators.getAssociativity(operator);
                ASTExpression rhs = associativity == LEFT ? parseExp(0) : parseExpression();
                exp = binary(operator.toString(), exp, rhs);
            } else if (lexer.readMatchingToken(TokenType.SEMICOLON)) {
                ASTExpression rhs = parseExp(0);
                exp = new ASTSequence(exp, rhs);
            } else {
                return exp;
            }
        }
    }

    private static boolean customOperator(Token token) {
        return token.type == OPERATOR && !token.value(Operator.class).isBuiltin();
    }

    private ASTExpression parseExp(int level) throws IOException {
        if (level <= operators.getMaxLevel())
            return parseExpN(level);
        else
            return parseApplicative();
    }

    private ASTExpression parseExpN(int level) throws IOException {
        ASTExpression exp = parseExp(level+1);

        while (true) {
            Operator op = lexer.readMatching(OPERATOR, operators.operator(level));
            if (op != null) {
                Associativity associativity = operators.getAssociativity(op);
                ASTExpression rhs = parseExp(associativity == LEFT ? level+1 : level);
                exp = binary(op.toString(), exp, rhs);
            } else {
                return exp;
            }
        }
    }

    private ASTExpression parseApplicative() throws IOException {
        ASTExpression exp = parsePrimitive();

        while (expressionStartTokens.contains(lexer.peekToken().type))
            exp = new ASTApplication(exp, parsePrimitive());

        return exp;
    }

    private ASTExpression parsePrimitive() throws IOException {
        switch (lexer.peekToken().type) {
        case EOF:       throw new SyntaxException("unexpected eof");
        case IF:        return parseIf();
        case LET:       return parseLet();
        case LAMBDA:    return parseLambda();
        case LPAREN:    return parseParens();
        case LBRACKET:  return parseList();
        case LITERAL:   return new ASTConstant(lexer.readToken().value);
        default:        return new ASTVariable(parseIdentifier());
        }
    }

    // if <expr> then <expr> else <expr>
    private ASTExpression parseIf() throws IOException {
        expectToken(TokenType.IF);
        ASTExpression test = parseExpression();
        expectToken(TokenType.THEN);
        ASTExpression cons = parseExpression();
        expectToken(TokenType.ELSE);
        ASTExpression alt = parseExpression();

        return new ASTIf(test, cons, alt);
    }

    // let [rec] <ident> <ident>* = <expr> in <expr>
    private ASTExpression parseLet() throws IOException {
        expectToken(TokenType.LET);
        boolean recursive = lexer.readMatchingToken(TokenType.REC);
        
        Symbol name = parseIdentifier();

        List<Symbol> args = new ArrayList<Symbol>();

        if (lexer.peekToken().type == OPERATOR) {
            Operator op = lexer.readToken().value(Operator.class);
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
    private ASTExpression parseLambda() throws IOException {
        expectToken(TokenType.LAMBDA);
        
        List<Symbol> args = new ArrayList<Symbol>();
        
        do {
            args.add(parseIdentifier());
        } while (!lexer.readMatchingToken(OPERATOR, Operator.RIGHT_ARROW));

        return new ASTLambda(args, parseExpression());
    }

    // () | (<op>) | ( <expr> )
    private ASTExpression parseParens() throws IOException {
        expectToken(TokenType.LPAREN);
        if (lexer.readMatchingToken(TokenType.RPAREN))
            return new ASTConstant(Unit.INSTANCE);
        
        if (lexer.peekTokenType() == TokenType.OPERATOR) {
            Operator op = lexer.readToken().value(Operator.class);
            expectToken(TokenType.RPAREN);
            return new ASTVariable(op.toString());
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
    private ASTExpression parseList() throws IOException {
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

    private Symbol parseIdentifier() throws IOException {
        Token token = lexer.readToken();

        switch (token.type) {
        case IDENTIFIER:
            return token.value(Symbol.class);
        case LPAREN:
            Token op = lexer.readToken();
            if (op.type == OPERATOR) {
                expectToken(TokenType.RPAREN);
                return op.value(Operator.class).toSymbol();
            } else
                throw new SyntaxException("expected operator name, got " + op);

        default:
            throw new SyntaxException("expected identifier, got " + token);
        }
    }

    private void expectToken(TokenType expected) throws IOException {
        Token token = lexer.readToken();

        if (expected != token.type)
            throw new SyntaxException("expected " + expected + " but got " + token);
    }

    private static ASTExpression binary(String op, ASTExpression lhs, ASTExpression rhs) {
        return new ASTApplication(new ASTApplication(new ASTVariable(symbol(op)), lhs), rhs);
    }
}
