package fi.evident.dojolisp.eval;

public class SyntaxException extends RuntimeException{
    public SyntaxException(String message) {
        super(message);
    }
}
