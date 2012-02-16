package komu.blunt.analyzer

import komu.blunt.ast.ASTExpression
import komu.blunt.core.CoreExpression
import komu.blunt.types.DataTypeDefinitions

object Analyzer {

    fun analyze(exp: ASTExpression, dataTypes: DataTypeDefinitions, env: StaticEnvironment): CoreExpression {
        val visitor = AnalyzingVisitor(dataTypes)
        val renamed = IdentifierRenamer.rename(exp).simplify()
        return visitor.analyze(renamed, env).sure()
    }
}
