package komu.blunt.parser;

import komu.blunt.ast.*;
import komu.blunt.eval.SyntaxException;
import komu.blunt.objects.Symbol;

import java.io.*;
import java.util.List;

import static java.util.Arrays.asList;
import static komu.blunt.objects.Symbol.symbol;

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
    
    public ASTExpression parseExpression() throws IOException {
        ASTExpression lhs = parseTerm();

        while (true) {
            if (lexer.readMatchingToken(Operator.EQUAL)) {
                ASTExpression rhs = parseTerm();
                lhs = binary("=", lhs, rhs);
            } else if (lexer.readMatchingToken(Operator.PLUS)) {
                ASTExpression rhs = parseTerm();
                lhs = binary("+", lhs, rhs);
            } else if (lexer.readMatchingToken(Operator.MINUS)) {
                ASTExpression rhs = parseTerm();
                lhs = binary("-", lhs, rhs);
            } else if (lexer.readMatchingToken(Token.SEMICOLON)) {
                ASTExpression rhs = parseTerm();
                lhs = new ASTSequence(lhs, rhs);
            } else {
                return lhs;
            }
        }
    }

    private ASTExpression parseTerm() throws IOException {
        ASTExpression lhs = parsePrimitive();

        while (true) {
            if (lexer.readMatchingToken(Operator.MULTIPLY)) {
                ASTExpression rhs = parsePrimitive();
                lhs = binary("*", lhs, rhs);
            } else if (lexer.readMatchingToken(Operator.DIVIDE)) {
                ASTExpression rhs = parsePrimitive();
                lhs = binary("/", lhs, rhs);
            } else {
                return lhs;
            }
        }
    }

    private ASTExpression parsePrimitive() throws IOException {
        Object token = lexer.readToken();

        if (token == Token.EOF)
            return null;

        if (token == Token.IF)
            return parseIf();

        if (token == Token.LET)
            return parseLet();

        if (token == Token.FN)
            return parseLambda();

        if (token == Token.LPAREN)
            return parseParens();

        if (token instanceof Number || token instanceof String)
            return new ASTConstant(token);

        if (token instanceof Symbol)
            return new ASTVariable((Symbol) token);

        throw new SyntaxException("invalid token: " + token);
    }

    // if <expr> then <expr> else <expr>
    private ASTExpression parseIf() throws IOException {
        ASTExpression test = parseExpression();
        expectToken(Token.THEN);
        ASTExpression cons = parseExpression();
        expectToken(Token.ELSE);
        ASTExpression alt = parseExpression();

        return new ASTIf(test, cons, alt);
    }

    // let [rec] <ident> = <expr> in <expr>
    private ASTExpression parseLet() throws IOException {
        boolean recursive = lexer.readMatchingToken(Token.REC);
        
        Symbol name = parseIdentifier();
        expectToken(Operator.EQUAL);
        ASTExpression value = parseExpression();
        expectToken(Token.IN);
        ASTExpression body = parseExpression();

        List<ASTBinding> bindings = asList(new ASTBinding(name, value));
        if (recursive)
            return new ASTLetRec(bindings, body);
        else
            return new ASTLet(bindings, body);
    }

    // fn <ident> -> expr
    private ASTExpression parseLambda() throws IOException {
        Symbol arg = parseIdentifier();
        expectToken(Operator.RIGHT_ARROW);
        ASTExpression body = parseExpression();
        
        return new ASTLambda(asList(arg), body);
    }

    // ( <expr> )
    private ASTExpression parseParens() throws IOException {
        ASTExpression exp = parseExpression();
        expectToken(Token.RPAREN);
        return exp;
    }

    private Symbol parseIdentifier() throws IOException {
        Object obj = lexer.readToken();
        if (obj instanceof Symbol)
            return (Symbol) obj;
        else
            throw new SyntaxException("expected identifier, got " + obj);
    }

    private void expectToken(Object expected) throws IOException {
        Object token = lexer.readToken();

        if (!expected.equals(token))
            throw new SyntaxException("expected " + expected + " but got " + token);
    }

    private static ASTExpression binary(String op, ASTExpression lhs, ASTExpression rhs) {
        return new ASTApplication(new ASTVariable(symbol(op)), lhs, rhs);
    }
}
