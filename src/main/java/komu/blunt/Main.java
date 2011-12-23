package komu.blunt;

import komu.blunt.eval.AnalyzationException;
import komu.blunt.eval.Evaluator;
import komu.blunt.eval.ResultWithType;
import komu.blunt.objects.EvaluationException;
import komu.blunt.objects.Symbol;

import java.io.FileNotFoundException;
import java.io.InputStream;

import static komu.blunt.objects.Symbol.symbol;

public class Main {
    
    private static final Symbol EXIT = symbol("exit");
    private static final Symbol DUMP = symbol("dump");

    public static void main(String[] args) throws Exception {
        Evaluator evaluator = new Evaluator();
        Prompt prompt = new Prompt();
        
        evaluator.load(openResource("prelude.lisp"));
        
        while (true) {
            try {
                Object form = prompt.readForm(">>> ");

                if (EXIT.equals(form)) {
                    break;
                } else if (DUMP.equals(form)) {
                    evaluator.dump();
                } else {
                    ResultWithType result = evaluator.evaluateWithType(form);
                    System.out.println(result);
                }
            } catch (AnalyzationException e) {
                System.out.println(e);
            } catch (EvaluationException e) {
                System.out.println(e);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static InputStream openResource(String path) throws FileNotFoundException {
        ClassLoader loader = Main.class.getClassLoader();

        InputStream in = loader.getResourceAsStream("prelude.lisp");
        if (in != null)
            return in;
        else
            throw new FileNotFoundException("file not found: " + path);
    }
}
