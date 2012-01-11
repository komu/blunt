package komu.blunt.asm;

import komu.blunt.eval.Environment;
import komu.blunt.objects.Procedure;

public enum Register {
    VAL, ENV, PROCEDURE, ARG, PC;

    public boolean isValidValue(Object value) {
        if (this == PROCEDURE) return value instanceof Procedure;
        if (this == PC) return value instanceof Integer;
        if (this == ENV) return value instanceof Environment;

        return true;
    }
}
