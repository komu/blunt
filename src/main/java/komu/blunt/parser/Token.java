package komu.blunt.parser;

public enum Token {
    EOF,
    IF("if"), THEN("then"), ELSE("else"), LET("let"), REC("rec"), IN("in"), FN("fn"),
    LPAREN("("), RPAREN(")"), SEMICOLON(";"), DOUBLE_SEMI(";;"), COMMA(","), LBRACKET("["), RBRACKET("]");

    private final String name;
    
    private Token() {
        this(null);
    }
    
    private Token(String name) {
        this.name = name;
    }

    public static Token keyword(String name) {
        for (Token token : values())
            if (name.equals(token.name))
                return token;
        return null;
    }

    @Override
    public String toString() {
        return name != null ? name : name();
    }
}
