package komu.blunt;

import komu.blunt.ast.ASTExpression;
import komu.blunt.parser.Parser;

import java.io.IOException;

public class Prompt {
    
    private final Parser parser = new Parser(System.in);
    
    public ASTExpression readExpression(String prompt) throws IOException {
        System.out.print(prompt);
        System.out.flush();
        return parser.parseExpression();
    }
}
