package komu.blunt.ast;

import komu.blunt.analyzer.Analyzer;
import komu.blunt.analyzer.StaticEnvironment;
import komu.blunt.analyzer.VariableReference;
import komu.blunt.core.CoreDefineExpression;
import komu.blunt.core.CoreExpression;
import komu.blunt.objects.Symbol;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ASTDefine {

    public final Symbol name;
    public final ASTExpression value;

    public ASTDefine(Symbol name, ASTExpression value) {
        this.name = checkNotNull(name);
        this.value = checkNotNull(value);
    }

    public CoreExpression analyze(StaticEnvironment rootEnv) {
        VariableReference var = rootEnv.define(name);

        return new CoreDefineExpression(Analyzer.analyze(value, rootEnv), var);
    }

    @Override
    public String toString() {
        return "(define " + name + " " + value + ")";
    }
}
