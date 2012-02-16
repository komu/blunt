package komu.blunt.parser;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class TokenType<T> {

    private static final Map<String,Keyword> keywords = new HashMap<>();
    
    public static final TokenType<Void> EOF = new TokenType<>(Void.class, "<eof>");
    public static final TokenType<Object> LITERAL = new TokenType<>(Object.class, "<literal>");
    public static final TokenType<String> IDENTIFIER = new TokenType<>(String.class, "<identifier>");
    public static final TokenType<String> TYPE_OR_CTOR_NAME = new TokenType<>(String.class, "<type or constructor name>");
    public static final TokenType<Operator> OPERATOR = new TokenType<>(Operator.class, "<operator>");
    public static final Keyword IF   = new Keyword("if");
    public static final Keyword THEN = new Keyword("then");
    public static final Keyword ELSE = new Keyword("else");
    public static final Keyword LET  = new Keyword("let");
    public static final Keyword REC  = new Keyword("rec");
    public static final Keyword IN   = new Keyword("in");
    public static final Keyword DATA = new Keyword("data");
    public static final Keyword CASE = new Keyword("case");
    public static final Keyword OF   = new Keyword("of");
    public static final Keyword DERIVING = new Keyword("deriving");

    public static final Punctuation LAMBDA = new Punctuation("\\");
    public static final Punctuation LPAREN = new Punctuation("(");
    public static final Punctuation RPAREN = new Punctuation(")");
    public static final Punctuation SEMICOLON = new Punctuation(";");
    public static final Punctuation END = new Punctuation("<end>");
    public static final Punctuation COMMA = new Punctuation(",");
    public static final Punctuation LBRACKET = new Punctuation("[");
    public static final Punctuation RBRACKET = new Punctuation("]");
    public static final Punctuation ASSIGN = new Punctuation("=");
    public static final Punctuation UNDERSCORE = new Punctuation("_");
    public static final Punctuation OR = new Punctuation("|");
    public static final Punctuation RIGHT_ARROW = new Punctuation("->");
    public static final Punctuation BIG_RIGHT_ARROW = new Punctuation("=>");

    public static class Punctuation extends TokenType<Void> {
        public Punctuation(String name) {
            super(Void.class, name);
        }
    }
    
    public static class Keyword extends TokenType<Void> {
        public Keyword(String name) {
            super(Void.class, name);
            
            keywords.put(name, this);
        }

        @Override
        public String toString() {
            return "keyword '" + name + "'";
        }
    }

    final String name;
    final Class<T> valueType;
    
    private TokenType(Class<T> valueType, String name) {
        this.valueType = checkNotNull(valueType);
        this.name = checkNotNull(name);
    }

    public static Keyword keyword(String name) {
        return keywords.get(name);
    }

    public Token<T> make(T value, SourceLocation location) {
        return new Token<T>(this, value, location);
    }

    @Override
    public String toString() {
        return name;
    }
}
