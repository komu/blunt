package komu.blunt.parser;

public enum TokenType {
    EOF, LITERAL, IDENTIFIER, OPERATOR,
    IF("if"), THEN("then"), ELSE("else"), LET("let"), REC("rec"), IN("in"), LAMBDA("\\"),
    LPAREN("("), RPAREN(")"), SEMICOLON(";"), DOUBLE_SEMI(";;"), COMMA(","), LBRACKET("["), RBRACKET("]"),
    ASSIGN("=");

    private final String name;
    
    private TokenType() {
        this(null);
    }
    
    private TokenType(String name) {
        this.name = name;
    }

    public static TokenType keyword(String name) {
        for (TokenType token : values())
            if (name.equals(token.name))
                return token;
        return null;
    }

    @Override
    public String toString() {
        return name != null ? name : name();
    }
}
