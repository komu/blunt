package fi.evident.dojolisp.asm;

import fi.evident.dojolisp.eval.Environment;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class CompoundProcedure {
    
    final Label label;
    final Environment env;

    public CompoundProcedure(Label label, Environment env) {
        this.label = requireNonNull(label);
        this.env = requireNonNull(env);
    }

    @Override
    public String toString() {
        return "<#CompoundProcedure " + label + ">";
    }
}
