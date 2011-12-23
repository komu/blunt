package komu.blunt.parser;

import komu.blunt.ast.ASTExpression;
import komu.blunt.reader.ASTBuilder;
import komu.blunt.reader.LispReader;
import komu.blunt.reader.Token;

import java.io.*;

public final class Parser {

    private final LispReader reader;

    public Parser(InputStream in) {
        this(new InputStreamReader(in));
    }

    public Parser(Reader reader) {
        this.reader = new LispReader(reader);
    }
    
    public ASTExpression parseExpression() throws IOException {
        Object form = reader.readForm();
        if (form != Token.EOF)
            return new ASTBuilder().parse(form);
        else
            return null;
    }

    public static ASTExpression parse(String input) {
        try {
            return new Parser(new StringReader(input)).parseExpression();
        } catch (IOException e) {
            throw new RuntimeException("unexpected IOException: " + e, e);
        }
    }
}
