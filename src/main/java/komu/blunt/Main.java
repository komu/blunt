package komu.blunt;

import komu.blunt.analyzer.AnalyzationException;
import komu.blunt.ast.ASTExpression;
import komu.blunt.ast.ASTVariable;
import komu.blunt.eval.Evaluator;
import komu.blunt.eval.ResultWithType;
import komu.blunt.objects.EvaluationException;
import komu.blunt.parser.SyntaxException;

import static komu.blunt.objects.Symbol.symbol;

public class Main {
    
    public static void main(String[] args) throws Exception {
        Evaluator evaluator = new Evaluator();
        
        evaluator.loadResource("prelude.blunt");
        
        for (String arg : args)
            evaluator.loadResource(arg);

        repl(evaluator);
    }

    private static void repl(Evaluator evaluator) {
        Prompt prompt = new Prompt();
        while (true) {
            try {
                ASTExpression exp = prompt.readExpression(">>> ");

                if (isSymbol("exit", exp)) {
                    break;
                } else if (isSymbol("dump", exp)) {
                    evaluator.dump();
                } else {
                    ResultWithType result = evaluator.evaluateWithType(exp);
                    System.out.println(result);
                }
            } catch (SyntaxException | AnalyzationException | EvaluationException e) {
                System.out.println(e);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean isSymbol(String symbol, ASTExpression exp) {
        if (exp instanceof ASTVariable) {
            ASTVariable constant = (ASTVariable) exp;
            return symbol(symbol).equals(constant.var);
        } else {
            return false;
        }
    }
}
