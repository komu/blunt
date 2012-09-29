package komu.blunt.analyzer

import komu.blunt.ast.ASTExpression
import komu.blunt.core.CoreExpression
import komu.blunt.types.DataTypeDefinitions

/**
 * Converts ASTExpressions to CoreExpressions.
 */
fun analyze(exp: ASTExpression, dataTypes: DataTypeDefinitions, env: StaticEnvironment): CoreExpression =
    AnalyzingVisitor(dataTypes).analyze(IdentifierRenamer.rename(exp).simplify(), env)
