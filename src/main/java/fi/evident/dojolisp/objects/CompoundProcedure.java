package fi.evident.dojolisp.objects;

import fi.evident.dojolisp.eval.Environment;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class CompoundProcedure {
    
    public final int address;
    public final Environment env;

    public CompoundProcedure(int address, Environment env) {
        this.address = address;
        this.env = requireNonNull(env);
    }

    @Override
    public String toString() {
        return "<#CompoundProcedure " + address + ">";
    }
}
