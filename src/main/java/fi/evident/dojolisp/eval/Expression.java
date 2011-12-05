package fi.evident.dojolisp.eval;

public abstract class Expression {
    public abstract Object evaluate(Environment env);
}
