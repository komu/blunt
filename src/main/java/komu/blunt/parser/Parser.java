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
import static komu.blunt.parser.Operator.EQUAL;

public final class Parser {

    private final Lexer lexer;
    private final OperatorSet operators = new OperatorSet();

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
        
        while (lexer.peekToken() != Token.EOF)
            result.add(parseDefinition());
        
        return result;
    }

    // <ident> <op> <ident> = <exp> ;;
    // <ident> <ident>* = <exp> ;;
    private ASTDefine parseDefinition() throws IOException {
        Symbol name = parseIdentifier();
        List<Symbol> args = new ArrayList<Symbol>();
        
        if (lexer.peekToken() instanceof Operator && !lexer.peekToken().toString().equals("=")) {
            Operator op = (Operator) lexer.readToken();
            args.add(name);
            args.add(parseIdentifier());
            name = symbol(op.toString());

            expectToken(EQUAL);
        } else {
            while (!lexer.readMatchingToken(EQUAL))
                args.add(parseIdentifier());
        }
        
        ASTExpression value = parseExpression();
        expectToken(Token.DOUBLE_SEMI);

        if (args.isEmpty())
            return new ASTDefine(name, value);
        else
            return new ASTDefine(name, new ASTLambda(args, value));
    }

    public ASTExpression parseExpression() throws IOException {
        ASTExpression exp = parseExp(0);

        while (true) {
            if (customOperator(lexer.peekToken())) {
                Operator operator = (Operator) lexer.readToken();
                Associativity associativity = operators.getAssociativity(operator);
                ASTExpression rhs = associativity == LEFT ? parseExp(0) : parseExpression();
                exp = binary(operator.toString(), exp, rhs);
            } else if (lexer.readMatchingToken(Token.SEMICOLON)) {
                ASTExpression rhs = parseExp(0);
                exp = new ASTSequence(exp, rhs);
            } else {
                return exp;
            }
        }
    }

    private static boolean customOperator(Object o) {
        return o instanceof Operator && !((Operator) o).isBuiltin();
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
            Operator op = lexer.readAnyMatchingToken(operators.operator(level));
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
        
        while (isExpressionStart(lexer.peekToken()))
            exp = new ASTApplication(exp, parsePrimitive());

        return exp;
    }

    private ASTExpression parsePrimitive() throws IOException {
        Object token = lexer.peekToken();

        if (token == Token.EOF)
            throw new SyntaxException("unexpected eof");
        else if (token == Token.IF)
            return parseIf();
        else if (token == Token.LET)
            return parseLet();
        else if (token == Token.LAMBDA)
            return parseLambda();
        else if (token == Token.LPAREN)
            return parseParens();
        else if (token == Token.LBRACKET)
            return parseList();
        else if (token instanceof Constant)
            return new ASTConstant(((Constant) lexer.readToken()).value);
        else
            return new ASTVariable(parseIdentifier());
    }

    private static final List<?> startTokens =
        asList(Token.IF, Token.LET, Token.LAMBDA, Token.LPAREN, Token.LBRACKET);

    private boolean isExpressionStart(Object o) {
        return startTokens.contains(o) || o instanceof Constant || o instanceof Symbol;
    }

    // if <expr> then <expr> else <expr>
    private ASTExpression parseIf() throws IOException {
        expectToken(Token.IF);
        ASTExpression test = parseExpression();
        expectToken(Token.THEN);
        ASTExpression cons = parseExpression();
        expectToken(Token.ELSE);
        ASTExpression alt = parseExpression();

        return new ASTIf(test, cons, alt);
    }

    // let [rec] <ident> = <expr> in <expr>
    private ASTExpression parseLet() throws IOException {
        expectToken(Token.LET);
        boolean recursive = lexer.readMatchingToken(Token.REC);
        
        Symbol name = parseIdentifier();
        expectToken(EQUAL);
        ASTExpression value = parseExpression();
        expectToken(Token.IN);
        ASTExpression body = parseExpression();

        List<ASTBinding> bindings = asList(new ASTBinding(name, value));
        if (recursive)
            return new ASTLetRec(bindings, body);
        else
            return new ASTLet(bindings, body);
    }

    // \ <ident> -> expr
    private ASTExpression parseLambda() throws IOException {
        expectToken(Token.LAMBDA);
        
        List<Symbol> args = new ArrayList<Symbol>();
        
        do {
            args.add(parseIdentifier());
        } while (!lexer.readMatchingToken(Operator.RIGHT_ARROW));

        return new ASTLambda(args, parseExpression());
    }

    // () | (<op>) | ( <expr> )
    private ASTExpression parseParens() throws IOException {
        expectToken(Token.LPAREN);
        if (lexer.readMatchingToken(Token.RPAREN))
            return new ASTConstant(Unit.INSTANCE);
        
        if (lexer.peekToken() instanceof Operator) {
            Operator op = (Operator) lexer.readToken();
            expectToken(Token.RPAREN);
            return new ASTVariable(op.toString());
        }
        
        List<ASTExpression> exps = new ArrayList<ASTExpression>();
        
        do {
            exps.add(parseExpression());    
        } while (lexer.readMatchingToken(Token.COMMA));

        expectToken(Token.RPAREN);
        
        if (exps.size() == 1)
            return exps.get(0);
        else
            return new ASTTuple(exps);
    }
    
    // []
    private ASTExpression parseList() throws IOException {
        expectToken(Token.LBRACKET);
        expectToken(Token.RBRACKET);
        
        return new ASTApplication(new ASTVariable(symbol("primitiveNil")), new ASTConstant(Unit.INSTANCE));
    }

    private Symbol parseIdentifier() throws IOException {
        Object obj = lexer.readToken();
        if (obj instanceof Symbol)
            return (Symbol) obj;
        if (obj == Token.LPAREN) {
            Object op = lexer.readToken();
            if (op instanceof Operator) {
                expectToken(Token.RPAREN);
                return Symbol.symbol(op.toString());
            } else
                throw new SyntaxException("expected operator name, got " + op);

        } else
            throw new SyntaxException("expected identifier, got " + obj);
    }

    private void expectToken(Object expected) throws IOException {
        Object token = lexer.readToken();

        if (!expected.equals(token))
            throw new SyntaxException("expected " + expected + " but got " + token);
    }

    private static ASTExpression binary(String op, ASTExpression lhs, ASTExpression rhs) {
        return new ASTApplication(new ASTApplication(new ASTVariable(symbol(op)), lhs), rhs);
    }
}
