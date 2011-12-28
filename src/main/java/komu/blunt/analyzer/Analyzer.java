package komu.blunt.analyzer;

import komu.blunt.ast.ASTExpression;
import komu.blunt.core.CoreExpression;

public final class Analyzer {

    private Analyzer() { }

    public static CoreExpression analyze(ASTExpression exp, StaticEnvironment env) {
        AnalyzingVisitor visitor = new AnalyzingVisitor();
        return visitor.analyze(exp, env);
    }
}
