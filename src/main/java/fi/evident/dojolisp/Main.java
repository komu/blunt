package fi.evident.dojolisp;

import fi.evident.dojolisp.eval.Evaluator;
import fi.evident.dojolisp.objects.Symbol;

import static fi.evident.dojolisp.objects.Symbol.symbol;

public class Main {
    
    private static final Symbol EXIT = symbol("exit");

    public static void main(String[] args) throws Exception {
        Evaluator evaluator = new Evaluator();
        Prompt prompt = new Prompt();

        while (true) {
            try {
                Object form = prompt.readForm(">>> ");

                if (EXIT.equals(form))
                    break;
                
                Object result = evaluator.evaluate(form);

                System.out.println(result);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }
}
