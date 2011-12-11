package fi.evident.dojolisp.asm;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class Linkage {
    
    public static final Linkage NEXT = new Linkage("next", null);
    public static final Linkage RETURN = new Linkage("return", null);

    private final String name;
    public final Label label;

    private Linkage(String name, Label label) {
        this.name = requireNonNull(name);
        this.label = label;
    }

    public static Linkage jump(Label label) {
        return new Linkage("jump", requireNonNull(label));
    }

    @Override
    public String toString() {
        return label != null ? name + " " + label : name;
    }
}
