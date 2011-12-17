package fi.evident.dojolisp;

import fi.evident.dojolisp.eval.AnalyzationException;
import fi.evident.dojolisp.eval.Evaluator;
import fi.evident.dojolisp.eval.ResultWithType;
import fi.evident.dojolisp.objects.EvaluationException;
import fi.evident.dojolisp.objects.Symbol;

import java.io.FileNotFoundException;
import java.io.InputStream;

import static fi.evident.dojolisp.objects.Symbol.symbol;

public class Main {
    
    private static final Symbol EXIT = symbol("exit");

    public static void main(String[] args) throws Exception {
        Evaluator evaluator = new Evaluator();
        Prompt prompt = new Prompt();
        
        evaluator.load(openResource("prologue.lisp"));
        
        while (true) {
            try {
                Object form = prompt.readForm(">>> ");

                if (EXIT.equals(form))
                    break;
                
                ResultWithType result = evaluator.evaluateWithType(form);

                System.out.println(result);
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

        InputStream in = loader.getResourceAsStream("prologue.lisp");
        if (in != null)
            return in;
        else
            throw new FileNotFoundException("file not found: " + path);
    }
}
