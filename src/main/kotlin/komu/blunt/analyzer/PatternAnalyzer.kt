package komu.blunt.analyzer

import komu.blunt.core.*
import komu.blunt.types.patterns.*

import java.util.ArrayList
import java.util.List

class PatternAnalyzer {

    fun createExtractor(pattern: Pattern, matchedObject: VariableReference, env: StaticEnvironment ): PatternExtractor {
        val predicate = pattern.accept(PredicateCreator(matchedObject, env), PatternPath.EMPTY.sure())
        val extractor = pattern.accept(ExtractorCreator(matchedObject, env), PatternPath.EMPTY.sure())
        return PatternExtractor(predicate, extractor)
    }

    class ExtractorCreator(val matchedObject: VariableReference, val env: StaticEnvironment) : PatternVisitor<PatternPath,CoreExpression> {

        override fun visit(pattern: WildcardPattern?, path: PatternPath): CoreExpression =
            CoreEmptyExpression.INSTANCE.sure()

        override fun visit(pattern: VariablePattern?, path: PatternPath): CoreExpression {
            val v = env.lookupInCurrentScopeOrDefine(pattern?.`var`)

            return CoreSetExpression(v, CoreExtractExpression(matchedObject, path))
        }

        override fun visit(pattern: ConstructorPattern?, path: PatternPath): CoreExpression {
            val exps = ArrayList<CoreExpression?>(1 + pattern?.args?.size().sure());

            var i = 0
            for (val p in pattern?.args)
                exps.add(p.accept(this, path.extend(i++).sure()));

            return CoreSequenceExpression(exps)
        }

        override fun visit(pattern: LiteralPattern?, path: PatternPath): CoreExpression =
            CoreEmptyExpression.INSTANCE.sure()
    }

    class PredicateCreator(val matchedObject: VariableReference, val env: StaticEnvironment) : PatternVisitor<PatternPath,CoreExpression> {
        override fun visit(pattern: WildcardPattern?, path: PatternPath): CoreExpression =
            CoreConstantExpression(true)

        override fun visit(pattern: VariablePattern?, path: PatternPath): CoreExpression =
            CoreConstantExpression(true)

        override fun visit(pattern: ConstructorPattern?, path: PatternPath): CoreExpression {
            val exps = ArrayList<CoreExpression?>(1 + pattern?.args?.size().sure());
            exps.add(matchesConstructor(path, pattern?.name.sure()));

            var i = 0
            for (val p in pattern?.args)
                exps.add(p.accept(this, path.extend(i++).sure()))

            return CoreExpression.and(exps).sure()
        }

        private fun matchesConstructor(path: PatternPath, name: String): CoreExpression =
            CoreEqualConstantExpression(name, CoreExtractTagExpression(matchedObject, path))

        override fun visit(pattern: LiteralPattern?, path: PatternPath): CoreExpression =
            CoreEqualConstantExpression(pattern?.value, CoreExtractExpression(matchedObject, path))
    }
}

