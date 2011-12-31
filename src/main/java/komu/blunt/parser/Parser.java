package komu.blunt.parser;

import com.google.common.collect.ImmutableList;
import komu.blunt.ast.*;
import komu.blunt.objects.Symbol;
import komu.blunt.types.*;
import komu.blunt.types.patterns.Pattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static komu.blunt.objects.Symbol.symbol;
import static komu.blunt.parser.Associativity.LEFT;
import static komu.blunt.parser.TokenType.*;
import static komu.blunt.types.Qualified.quantify;
import static komu.blunt.types.Type.functionType;

public final class Parser {

    private final Lexer lexer;
    private final OperatorSet operators = new OperatorSet();
    private final TypeParser typeParser;
    private final PatternParser patternParser;

    @SuppressWarnings("unchecked")
    private static final List<TokenType<?>> expressionStartTokens =
        Arrays.asList(IF, LET, LAMBDA, LPAREN, LBRACKET, LITERAL, IDENTIFIER, TYPE_OR_CTOR_NAME, CASE);

    public Parser(String source) {
        this.lexer = new Lexer(source);
        this.typeParser = new TypeParser(lexer);
        this.patternParser = new PatternParser(lexer);
    }

    public static ASTExpression parseExpression(String source) {
        return new Parser(source).parseExpression();
    }
    
    public List<ASTDefinition> parseDefinitions() {
        List<ASTDefinition> result = new ArrayList<ASTDefinition>();
        
        while (lexer.peekTokenType() != EOF)
            result.add(parseDefinition());
        
        return result;
    }
    
    private ASTDefinition parseDefinition() {
        if (lexer.nextTokenIs(DATA))
            return parseDataDefinition();
        else
            return parseValueDefinition();
    }

    private ASTDataDefinition parseDataDefinition() {
        lexer.expectIndentStartToken(DATA);

        String name = lexer.readToken(TYPE_OR_CTOR_NAME).value;
        
        List<TypeVariable> vars = new ArrayList<TypeVariable>();
        while (!lexer.nextTokenIs(ASSIGN))
            vars.add(typeParser.parseTypeVariable());

        lexer.expectToken(ASSIGN);

        ImmutableList.Builder<ConstructorDefinition> constructors = ImmutableList.builder();
        
        Type resultType = Type.genericType(name, vars);
        
        do {
            constructors.add(parseConstructorDefinition(vars, resultType));
        } while (lexer.readMatchingToken(OR));

        lexer.expectToken(END);

        return AST.data(name, constructors.build());
    }

    private ConstructorDefinition parseConstructorDefinition(Collection<TypeVariable> vars, Type resultType) {
        String name = lexer.readToken(TYPE_OR_CTOR_NAME).value;
        
        List<Type> args = new ArrayList<Type>();
        while (!lexer.nextTokenIs(OR) && !lexer.nextTokenIs(END))
            args.add(typeParser.parseTypePrimitive());

        Qualified<Type> type = new Qualified<Type>(functionType(args, resultType));
        return new ConstructorDefinition(name, quantify(vars, type), args.size());
    }

    // <ident> <op> <ident> = <exp> ;;
    // <ident> <ident>* = <exp> ;;
    private ASTValueDefinition parseValueDefinition() {
        lexer.pushBlockStartAtNextToken();

        Symbol name = parseIdentifier();
        List<Symbol> args = new ArrayList<Symbol>();
        
        if (lexer.nextTokenIs(OPERATOR)) {
            Operator op = lexer.readToken(OPERATOR).value;
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
            if (customOperator(lexer.peekToken())) {
                Operator operator = lexer.readToken(OPERATOR).value;
                Associativity associativity = operators.getAssociativity(operator);
                ASTExpression rhs = associativity == LEFT ? parseExp(0) : parseExpression();
                exp = binary(operator, exp, rhs);
            } else if (lexer.readMatchingToken(SEMICOLON)) {
                ASTExpression rhs = parseExp(0);
                exp = AST.sequence(exp, rhs);
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
        ASTExpression exp = parseExp(level + 1);

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
            return AST.constant(lexer.readToken(LITERAL).value);

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
            Operator op = lexer.readToken(OPERATOR).value;
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

        List<ImplicitBinding> bindings = asList(new ImplicitBinding(name, value));
        if (recursive)
            return AST.letRec(bindings, body);
        else
            return AST.let(bindings, body);
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
            Operator op = lexer.readToken(OPERATOR).value;
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
        ASTList list = new ASTList();

        lexer.expectToken(LBRACKET);

        if (lexer.peekTokenType() != RBRACKET) {
            do {
                list.add(parseExpression());
            } while (lexer.readMatchingToken(COMMA));
        }

        lexer.expectToken(RBRACKET);

        return list;
    }

    private ASTExpression parseVariableOrConstructor() {
        Token<?> token = lexer.readToken();

        if (token.type == IDENTIFIER)
            return AST.variable(token.asType(IDENTIFIER).value);

        if (token.type == TYPE_OR_CTOR_NAME)
            return AST.constructor(token.asType(TYPE_OR_CTOR_NAME).value);

        if (token.type == LPAREN) {
            Operator op = lexer.readToken(OPERATOR).value;
            lexer.expectToken(RPAREN);

            if (op.isConstructor())
                return AST.constructor(op.toString());
            else
                return AST.variable(op.toString());
        }

        throw parseError("expected identifier or type constructor, got " + token);
    }

    private Symbol parseIdentifier() {
        Token<?> token = lexer.readToken();

        if (token.type == IDENTIFIER)
            return symbol(token.asType(IDENTIFIER).value);

        if (token.type == LPAREN) {
            Token<Operator> op = lexer.readToken(OPERATOR);
            lexer.expectToken(RPAREN);
            return op.asType(OPERATOR).value.toSymbol();
        }

        throw parseError("expected identifier, got " + token);
    }

    private SyntaxException parseError(final String s) {
        return lexer.parseError(s);
    }

    private static ASTExpression binary(Operator op, ASTExpression lhs, ASTExpression rhs) {
        ASTExpression exp = op.isConstructor() ? AST.constructor(op.toString()) : AST.variable(op.toString());
        
        return AST.apply(exp, lhs, rhs);
    }
}
