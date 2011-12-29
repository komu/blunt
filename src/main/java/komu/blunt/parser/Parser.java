package komu.blunt.parser;

import com.google.common.collect.ImmutableList;
import komu.blunt.ast.*;
import komu.blunt.objects.Symbol;
import komu.blunt.objects.Unit;
import komu.blunt.types.*;
import komu.blunt.types.patterns.ConstructorPattern;
import komu.blunt.types.patterns.LiteralPattern;
import komu.blunt.types.patterns.Pattern;
import komu.blunt.types.patterns.VariablePattern;

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

    @SuppressWarnings("unchecked")
    private static final List<TokenType<?>> expressionStartTokens =
        Arrays.asList(IF, LET, LAMBDA, LPAREN, LBRACKET, LITERAL, IDENTIFIER, TYPE_OR_CTOR_NAME, CASE);

    public Parser(String source) {
        this.lexer = new Lexer(source);
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
            vars.add(parseTypeVariable());

        lexer.expectToken(ASSIGN);

        ImmutableList.Builder<ConstructorDefinition> constructors = ImmutableList.builder();
        
        Type resultType = Type.genericType(name, vars);
        
        do {
            constructors.add(parseConstructorDefinition(vars, resultType));
        } while (lexer.readMatchingToken(OR));

        lexer.expectToken(END);

        return new ASTDataDefinition(name, constructors.build());
    }

    private ConstructorDefinition parseConstructorDefinition(Collection<TypeVariable> vars, Type resultType) {
        String name = lexer.readToken(TYPE_OR_CTOR_NAME).value;
        
        List<Type> args = new ArrayList<Type>();
        while (!lexer.nextTokenIs(OR) && !lexer.nextTokenIs(END))
            args.add(parseType());

        Qualified<Type> type = new Qualified<Type>(functionType(args, resultType));
        return new ConstructorDefinition(name, quantify(vars, type), args.size());
    }

    private Type parseType() {
        return parseTypeVariable(); // TODO: support concrete types
    }

    private TypeVariable parseTypeVariable() {
        Symbol name = parseIdentifier(); // TODO: not really correct, but will do for now
        return new TypeVariable(name.toString(), Kind.STAR);
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
            return new ASTValueDefinition(name, value);
        else
            return new ASTValueDefinition(name, new ASTLambda(args, value));
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
            exp = new ASTApplication(exp, parsePrimitive());

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
            return new ASTConstant(lexer.readToken(LITERAL).value);

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

        return new ASTIf(test, cons, alt);
    }

    // case <exp> of <alternative> (| <alternative>)*
    private ASTExpression parseCase() {
        lexer.expectToken(CASE);
        ASTExpression exp = parseExpression();
        lexer.expectToken(OF);

        ImmutableList.Builder<ASTAlternative> alts = ImmutableList.builder();
        do {
            lexer.expectToken(OR);
            alts.add(parseAlternative());
        } while (lexer.nextTokenIs(OR));

        return new ASTCase(exp, alts.build());
    }

    // <pattern> -> <exp>
    private ASTAlternative parseAlternative() {
        Pattern pattern = parsePatternFollowedBy(RIGHT_ARROW);
        lexer.expectToken(RIGHT_ARROW);
        ASTExpression exp = parseExpression();
        
        return new ASTAlternative(pattern, exp);
    }

    // <literal> | <variable> | ( <pattern> ) | <constructor> <pattern>* |
    private Pattern parsePatternFollowedBy(TokenType endToken) {
        if (lexer.nextTokenIs(LITERAL)) {
            return new LiteralPattern(lexer.readToken(LITERAL).value);

        } else if (lexer.nextTokenIs(IDENTIFIER)) {
            return new VariablePattern(symbol(lexer.readToken(IDENTIFIER).value));

        } else if (lexer.readMatchingToken(LPAREN)) {
            Pattern pattern = parsePatternFollowedBy(RPAREN);
            lexer.expectToken(RPAREN);
            return pattern;

        } else if (lexer.nextTokenIs(TYPE_OR_CTOR_NAME)) {
            String name = lexer.readToken(TYPE_OR_CTOR_NAME).value;
            ImmutableList.Builder<Pattern> args = ImmutableList.builder();
            while (!lexer.nextTokenIs(endToken))
                args.add(parsePatternFollowedBy(endToken));
            
            return new ConstructorPattern(name, args.build());
            
        } else {
            throw parseError("expected pattern");
        }
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
            value = new ASTLambda(args, value);

        List<ImplicitBinding> bindings = asList(new ImplicitBinding(name, value));
        if (recursive)
            return new ASTLetRec(bindings, body);
        else
            return new ASTLet(bindings, body);
    }

    // \ <ident> -> expr
    private ASTExpression parseLambda() {
        lexer.expectToken(LAMBDA);

        List<Symbol> args = new ArrayList<Symbol>();
        
        do {
            args.add(parseIdentifier());
        } while (!lexer.readMatchingToken(RIGHT_ARROW));

        return new ASTLambda(args, parseExpression());
    }

    // () | (<op>) | ( <expr> )
    private ASTExpression parseParens() {
        lexer.expectToken(LPAREN);
        if (lexer.readMatchingToken(RPAREN))
            return new ASTConstant(Unit.INSTANCE);
        
        if (lexer.nextTokenIs(OPERATOR)) {
            Operator op = lexer.readToken(OPERATOR).value;
            lexer.expectToken(RPAREN);

            return op.isConstructor() ? new ASTConstructor(op.toString()) : new ASTVariable(op.toString());
        }
        
        List<ASTExpression> exps = new ArrayList<ASTExpression>();
        
        do {
            exps.add(parseExpression());    
        } while (lexer.readMatchingToken(COMMA));

        lexer.expectToken(RPAREN);

        if (exps.size() == 1)
            return exps.get(0);
        else
            return new ASTTuple(exps);
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
            return new ASTVariable(token.asType(IDENTIFIER).value);

        if (token.type == TYPE_OR_CTOR_NAME)
            return new ASTConstructor(token.asType(TYPE_OR_CTOR_NAME).value);

        if (token.type == LPAREN) {
            Operator op = lexer.readToken(OPERATOR).value;
            lexer.expectToken(RPAREN);

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
        ASTExpression exp = op.isConstructor() ? new ASTConstructor(op.toString()) : new ASTVariable(op.toString());
        return new ASTApplication(new ASTApplication(exp, lhs), rhs);
    }
}
