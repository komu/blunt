package komu.blunt;

import komu.blunt.ast.ASTConstant;
import komu.blunt.ast.ASTExpression;
import komu.blunt.eval.AnalyzationException;
import komu.blunt.eval.Evaluator;
import komu.blunt.eval.ResultWithType;
import komu.blunt.eval.SyntaxException;
import komu.blunt.objects.EvaluationException;

import java.io.FileNotFoundException;
import java.io.InputStream;

import static komu.blunt.objects.Symbol.symbol;

public class Main {

    public static void main(String[] args) throws Exception {
        Evaluator evaluator = new Evaluator();
        Prompt prompt = new Prompt();
        
        evaluator.load(openResource("prelude.blunt"));
        
        while (true) {
            try {
                ASTExpression exp = prompt.readExpression(">>> ");

                if (isConstant("exit", exp)) {
                    break;
                } else if (isConstant("dump", exp)) {
                    evaluator.dump();
                } else {
                    ResultWithType result = evaluator.evaluateWithType(exp);
                    System.out.println(result);
                }
            } catch (SyntaxException e) {
                System.out.println(e);
            } catch (AnalyzationException e) {
                System.out.println(e);
            } catch (EvaluationException e) {
                System.out.println(e);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean isConstant(String symbol, ASTExpression exp) {
        if (exp instanceof ASTConstant) {
            ASTConstant constant = (ASTConstant) exp;
            return symbol(symbol).equals(constant.value);
        } else {
            return false;
        }
    }

    private static InputStream openResource(String path) throws FileNotFoundException {
        ClassLoader loader = Main.class.getClassLoader();

        InputStream in = loader.getResourceAsStream("prelude.blunt");
        if (in != null)
            return in;
        else
            throw new FileNotFoundException("file not found: " + path);
    }
}
