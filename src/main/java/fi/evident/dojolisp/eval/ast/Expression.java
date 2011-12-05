package fi.evident.dojolisp.eval.ast;

import fi.evident.dojolisp.eval.Environment;
import fi.evident.dojolisp.eval.types.Type;

public abstract class Expression {
    public abstract Object evaluate(Environment env);
    public abstract Type typeCheck();
}
