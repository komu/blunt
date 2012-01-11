package komu.blunt.core;

import komu.blunt.analyzer.VariableReference;
import komu.blunt.asm.Assembler;
import komu.blunt.asm.Instructions;
import komu.blunt.asm.Linkage;
import komu.blunt.asm.Register;

import static com.google.common.base.Preconditions.checkNotNull;

public class CoreExtractTagExpression extends CoreExpression {

    private final PatternPath path;
    private final VariableReference var;

    public CoreExtractTagExpression(VariableReference var, PatternPath path) {
        this.var = checkNotNull(var);
        this.path = checkNotNull(path);
    }

    @Override
    public Instructions assemble(Assembler asm, Register target, Linkage linkage) {
        Instructions instructions = new Instructions();
        instructions.loadVariable(target, var);
        instructions.loadTag(target, target, path);
        instructions.finishWithLinkage(linkage);
        return instructions;
    }

    @Override
    public CoreExpression simplify() {
        return this;
    }

    @Override
    public String toString() {
        return "(extract-tag " + var.name + " " + path + ")";
    }
}
