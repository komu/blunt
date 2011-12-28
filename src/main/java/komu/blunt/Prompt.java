package komu.blunt;

import komu.blunt.ast.ASTExpression;
import komu.blunt.parser.Parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Prompt {
    
    private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    
    public ASTExpression readExpression(String prompt) throws IOException {
        System.out.print(prompt);
        System.out.flush();

        return Parser.parseExpression(reader.readLine());
    }
}
