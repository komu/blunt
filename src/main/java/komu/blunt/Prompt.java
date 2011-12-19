package komu.blunt;

import komu.blunt.reader.LispReader;

import java.io.IOException;

public class Prompt {
    
    private final LispReader reader = new LispReader(System.in);
    
    public Object readForm(String prompt) throws IOException {
        System.out.print(prompt);
        System.out.flush();
        return reader.readForm();
    }
}
