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
    public void assemble(Assembler asm, Instructions instructions, Register target, Linkage linkage) {
        instructions.loadVariable(target, var);
        instructions.loadExtracted(target, target, path);
        instructions.finishWithLinkage(linkage);
    }

    @Override
    public String toString() {
        return "(extract " + path + ")";
    }
}
