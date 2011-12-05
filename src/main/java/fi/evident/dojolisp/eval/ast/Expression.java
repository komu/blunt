package fi.evident.dojolisp.eval.ast;

import fi.evident.dojolisp.eval.Environment;

public abstract class Expression {
    public abstract Object evaluate(Environment env);
}
