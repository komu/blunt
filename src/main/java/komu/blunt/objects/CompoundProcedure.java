package komu.blunt.objects;

import komu.blunt.eval.Environment;

import static komu.blunt.utils.Objects.requireNonNull;

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
