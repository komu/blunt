package komu.blunt.reader;

import java.util.HashMap;
import java.util.Map;

public enum Token {
    EOF, QUOTE, LPAREN, RPAREN, EQ, IF, THEN, ELSE, LET, REC, IN, FN, RIGHT_ARROW;

    private static final Map<String, Token> keywords = createKeywordMap();

    public static Token keyword(String name) {
        return keywords.get(name);
    }

    private static Map<String, Token> createKeywordMap() {
        Map<String, Token> keywords = new HashMap<String, Token>();
        keywords.put("if", IF);
        keywords.put("then", THEN);
        keywords.put("else", ELSE);
        keywords.put("let", LET);
        keywords.put("rec", REC);
        keywords.put("in", IN);
        keywords.put("=", EQ);
        keywords.put("fn", FN);
        keywords.put("->", RIGHT_ARROW);
        return keywords;
    }
}
