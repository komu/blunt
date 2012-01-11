package komu.blunt.core;

import komu.blunt.analyzer.VariableReference;
import komu.blunt.asm.Assembler;
import komu.blunt.asm.Instructions;
import komu.blunt.asm.Linkage;
import komu.blunt.asm.Register;

import static com.google.common.base.Preconditions.checkNotNull;

public final class CoreExtractExpression extends CoreExpression {

    private final VariableReference var;
    private final PatternPath path;

    public CoreExtractExpression(VariableReference var, PatternPath path) {
        this.var = checkNotNull(var);
        this.path = checkNotNull(path);
    }

    @Override
    public Instructions assemble(Assembler asm, Register target, Linkage linkage) {
        Instructions instructions = new Instructions();
        instructions.loadVariable(target, var);
        instructions.loadExtracted(target, target, path);
        instructions.finishWithLinkage(linkage);
        return instructions;
    }

    @Override
    public String toString() {
        return "(extract " + path + ")";
    }
}
