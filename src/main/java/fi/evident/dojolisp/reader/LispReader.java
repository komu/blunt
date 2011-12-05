package fi.evident.dojolisp.reader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static fi.evident.dojolisp.objects.Symbol.symbol;
import static java.util.Arrays.asList;

public final class LispReader {

    private final LispTokenizer tokenizer;

    public LispReader(InputStream in) {
        this(new InputStreamReader(in));
    }

    public LispReader(Reader reader) {
        this.tokenizer = new LispTokenizer(reader);
    }
    
    public static Object parse(String input) {
        try {
            return new LispReader(new StringReader(input)).readForm();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Object readForm() throws IOException {
        Object token = readForm1();
        if (token == Token.RPAREN)
            throw new RuntimeException("Unexpected )");
        else
            return token;
    }
    
    private Object readForm1() throws IOException {
        Object token = tokenizer.readToken();
        if (token == Token.QUOTE)
            return asList(symbol("quote"), readForm1());
        else if (token == Token.LPAREN) 
            return readList();
        else
            return token;
    }

    private List<Object> readList() throws IOException {
        List<Object> list = new ArrayList<Object>();
        
        while (true) {
            Object object = readForm1();
            if (object == Token.EOF)
                throw new RuntimeException("Unexpected EOF");
            else if (object == Token.RPAREN)
                break;
            else
                list.add(object);
        }
        
        return list;
    }
}
