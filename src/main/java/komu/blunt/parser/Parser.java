package komu.blunt.parser;

import com.google.common.collect.ImmutableList;
import komu.blunt.ast.*;
import komu.blunt.objects.Symbol;
import komu.blunt.types.DataTypeDefinitions;
import komu.blunt.types.patterns.Pattern;
import komu.blunt.types.patterns.VariablePattern;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
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
        asList(IF, LET, LAMBDA, LPAREN, LBRACKET, LITERAL, IDENTIFIER, TYPE_OR_CTOR_NAME, CASE);

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
        List<ASTDefinition> result = new ArrayList<>();
        
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


    private ASTValueDefinition parseValueDefinition() {
        LexerState lexerState = lexer.save();
        try {
            try {
                return parseSimpleDefinition();
            } catch (SyntaxException e) {
                lexer.restore(lexerState);
                return parseOperatorDefinition();
            }

        } catch (SyntaxException e) {
            lexer.restore(lexerState);
            return parseNormalValueDefinition();
        }
    }

    private ASTValueDefinition parseSimpleDefinition() {
        lexer.pushBlockStartAtNextToken();

        Symbol name = parseIdentifier();

        lexer.expectToken(ASSIGN);
        ASTExpression value = parseExpression();
        lexer.expectToken(END);
        return AST.define(name, value);
    }

    // <pattern> <op> <pattern> = <exp> ;;
    private ASTValueDefinition parseOperatorDefinition() {
        lexer.pushBlockStartAtNextToken();
        
        Pattern left = patternParser.parseSimplePattern();
        Operator op = lexer.readTokenValue(OPERATOR);
        Pattern right = patternParser.parseSimplePattern();
     
        lexer.expectToken(ASSIGN);

        ASTExpression value = parseExpression();
        lexer.expectToken(END);

        FunctionBuilder functionBuilder = new FunctionBuilder();
        functionBuilder.addAlternative(ImmutableList.of(left, right), value);
        return AST.define(op.toSymbol(), functionBuilder.build());
    }
    
    // <ident> <pattern>+ = <exp> ;;
    private ASTValueDefinition parseNormalValueDefinition() {
        FunctionBuilder functionBuilder = new FunctionBuilder();

        Symbol name = null;
        while (name == null || nextTokenIsIdentifier(name)) {
            lexer.pushBlockStartAtNextToken();
            name = parseIdentifier();

            ImmutableList.Builder<Pattern> argsBuilder = ImmutableList.builder();

            while (!lexer.nextTokenIs(ASSIGN))
                argsBuilder.add(patternParser.parseSimplePattern());

            lexer.expectToken(ASSIGN);

            ASTExpression value = parseExpression();
            lexer.expectToken(END);
            functionBuilder.addAlternative(argsBuilder.build(), value);
        }

        return AST.define(name, functionBuilder.build());
    }

    private boolean nextTokenIsIdentifier(Symbol name) {
        return lexer.nextTokenIs(IDENTIFIER) && lexer.peekTokenValue(IDENTIFIER).equals(name.toString());
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

        List<Symbol> args = new ArrayList<>();

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

        ImmutableList.Builder<Pattern> argsBuilder = ImmutableList.builder();

        do {
            argsBuilder.add(patternParser.parseSimplePattern());
        } while (!lexer.readMatchingToken(RIGHT_ARROW));
        
        FunctionBuilder builder = new FunctionBuilder();
        ImmutableList<Pattern> patterns = argsBuilder.build();
        builder.addAlternative(patterns, parseExpression());
        return builder.build();
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

        List<ASTExpression> exps = new ArrayList<>();
        
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
    
    private static class FunctionBuilder {
        private final List<Symbol> symbols = new ArrayList<>();
        private final List<ASTExpression> exps = new ArrayList<>();
        private final ImmutableList.Builder<ASTAlternative> alternatives = ImmutableList.builder();
        
        void addAlternative(ImmutableList<Pattern> args, ASTExpression body) {
            if (exps.isEmpty()) {
                for (int i = 0; i < args.size(); i++) {
                    Symbol var = symbol("$arg" + i); // TODO: fresh symbols
                    symbols.add(var);
                    exps.add(AST.variable(var));
                }                            
            } else if (args.size() != exps.size()) {
                throw new SyntaxException("invalid amount of arguments");
            }
            
            alternatives.add(AST.alternative(Pattern.tuple(args), body));
        }
        
        ASTExpression build() {
            ImmutableList<ASTAlternative> alts = alternatives.build();
            
            // optimization
            List<Symbol> simpleVars = containsOnlyVariablePatterns(alts);
            if (simpleVars != null)
                return AST.lambda(simpleVars, alts.get(0).value);

            return AST.lambda(symbols, AST.caseExp(AST.tuple(exps), alts));
        }

        private List<Symbol> containsOnlyVariablePatterns(ImmutableList<ASTAlternative> alts) {
            if (alts.size() == 1)
                return variablePattern(alts.get(0).pattern);
            return null;
        }

        private List<Symbol> variablePattern(Pattern pattern) {
            if (pattern instanceof VariablePattern)
                return singletonList(((VariablePattern) pattern).var);

            /*
            if (pattern instanceof ConstructorPattern) {
                ConstructorPattern c = (ConstructorPattern) pattern;

                if (c.name.equals("()"))
                    return null;
                if (!c.name.equals(DataTypeDefinitions.tupleName(c.args.size())))
                    return null;

                List<Symbol> vars = new ArrayList<>(c.args.size());

                for (Pattern p : c.args)
                    if (p instanceof VariablePattern) {
                        vars.add(((VariablePattern) p).var);
                    } else {
                        return null;
                    }

                return vars;
            }
            */

            return null;
        }
    }
}
