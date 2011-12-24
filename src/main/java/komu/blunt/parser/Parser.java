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
import static komu.blunt.parser.Operator.*;

public final class Parser {

    private final Lexer lexer;

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

    // <ident> = <expr> ;;
    private ASTDefine parseDefinition() throws IOException {
        Symbol name = parseIdentifier();
        List<Symbol> args = new ArrayList<Symbol>();
        
        while (!lexer.readMatchingToken(EQUAL))
            args.add(parseIdentifier());
        
        ASTExpression value = parseExpression();
        expectToken(Token.DOUBLE_SEMI);

        if (args.isEmpty())
            return new ASTDefine(name, value);
        else
            return new ASTDefine(name, new ASTLambda(args, value));
    }

    public ASTExpression parseExpression() throws IOException {
        ASTExpression exp = parseExp2();

        while (true) {
            if (lexer.peekToken() instanceof Operator && !((Operator) lexer.peekToken()).isBuiltin()) {
                Operator operator = (Operator) lexer.readToken();
                ASTExpression rhs = parseExp2();
                exp = binary(operator.toString(), exp, rhs);
            } else if (lexer.readMatchingToken(Token.SEMICOLON)) {
                ASTExpression rhs = parseExp2();
                exp = new ASTSequence(exp, rhs);
            } else {
                return exp;
            }
        }
    }

    private ASTExpression parseExp2() throws IOException {
        ASTExpression exp = parseExp3();

        while (true) {
            Operator op = lexer.readAnyMatchingToken(EQUAL, LT, LE, GT, GE);
            if (op != null) {
                ASTExpression rhs = parseExp3();
                exp = binary(op.toString(), exp, rhs);
            } else {
                return exp;
            }
        }
    }

    private ASTExpression parseExp3() throws IOException {
        ASTExpression exp = parseExp4();

        while (true) {
            Operator op = lexer.readAnyMatchingToken(PLUS, MINUS);
            if (op != null) {
                ASTExpression rhs = parseExp4();
                exp = binary(op.toString(), exp, rhs);
            } else {
                return exp;
            }
        }
    }

    private ASTExpression parseExp4() throws IOException {
        ASTExpression exp = parseExp5();

        while (true) {
            if (lexer.readMatchingToken(Operator.MULTIPLY)) {
                ASTExpression rhs = parseExp5();
                exp = binary("*", exp, rhs);
            } else if (lexer.readMatchingToken(Operator.DIVIDE)) {
                ASTExpression rhs = parseExp5();
                exp = binary("/", exp, rhs);
            } else {
                return exp;
            }
        }
    }

    private ASTExpression parseExp5() throws IOException {
        ASTExpression exp = parseExp6();
        
        while (isExpressionStart(lexer.peekToken()))
            exp = new ASTApplication(exp, parseExp6());

        return exp;
    }

    private static final List<?> startTokens =
        asList(Token.IF, Token.LET, Token.LAMBDA, Token.LPAREN, Token.LBRACKET);

    private boolean isExpressionStart(Object o) {
        return startTokens.contains(o) || o instanceof Constant || o instanceof Symbol;
    }

    private ASTExpression parseExp6() throws IOException {
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
