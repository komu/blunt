package komu.blunt.analyzer;

import komu.blunt.ast.ASTExpression;
import komu.blunt.core.CoreExpression;
import komu.blunt.types.DataTypeDefinitions;

public final class Analyzer {

    private Analyzer() { }

    public static CoreExpression analyze(ASTExpression exp, DataTypeDefinitions dataTypes, StaticEnvironment env) {
        AnalyzingVisitor visitor = new AnalyzingVisitor(dataTypes);
        ASTExpression renamed = IdentifierRenamer.rename(exp);
        return visitor.analyze(renamed, env);
    }
}
