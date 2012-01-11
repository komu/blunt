package komu.blunt.analyzer;

import komu.blunt.core.*;
import komu.blunt.types.patterns.*;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

final class PatternAnalyzer {

    public PatternExtractor createExtractor(Pattern pattern, VariableReference matchedObject, StaticEnvironment env) {
        CoreExpression predicate = pattern.accept(new PredicateCreator(matchedObject, env), PatternPath.EMPTY);
        CoreExpression extractor = pattern.accept(new ExtractorCreator(matchedObject, env), PatternPath.EMPTY);
        return new PatternExtractor(predicate, extractor);
    }

    private static final class ExtractorCreator implements PatternVisitor<PatternPath,CoreExpression> {
        private final VariableReference matchedObject;
        private final StaticEnvironment env;

        ExtractorCreator(VariableReference matchedObject, StaticEnvironment env) {
            this.matchedObject = checkNotNull(matchedObject);
            this.env = checkNotNull(env);
        }

        @Override
        public CoreExpression visit(WildcardPattern pattern, PatternPath path) {
            return CoreEmptyExpression.INSTANCE;
        }

        @Override
        public CoreExpression visit(VariablePattern pattern, PatternPath path) {
            VariableReference var = env.lookupInCurrentScopeOrDefine(pattern.var);

            return new CoreSetExpression(var, new CoreExtractExpression(matchedObject, path));
        }

        @Override
        public CoreExpression visit(ConstructorPattern pattern, PatternPath path) {
            List<CoreExpression> exps = new ArrayList<>(1 + pattern.args.size());

            int i = 0;
            for (Pattern p : pattern.args)
                exps.add(p.accept(this, path.extend(i++)));

            return new CoreSequenceExpression(exps);
        }

        @Override
        public CoreExpression visit(LiteralPattern pattern, PatternPath path) {
            return CoreEmptyExpression.INSTANCE;
        }
    }

    private static final class PredicateCreator implements PatternVisitor<PatternPath,CoreExpression> {
        private final VariableReference matchedObject;
        private final StaticEnvironment env;

        PredicateCreator(VariableReference matchedObject, StaticEnvironment env) {
            this.matchedObject = checkNotNull(matchedObject);
            this.env = checkNotNull(env);
        }

        @Override
        public CoreExpression visit(WildcardPattern pattern, PatternPath path) {
            return new CoreConstantExpression(true);
        }

        @Override
        public CoreExpression visit(VariablePattern pattern, PatternPath path) {
            return new CoreConstantExpression(true);
        }

        @Override
        public CoreExpression visit(ConstructorPattern pattern, PatternPath path) {
            List<CoreExpression> exps = new ArrayList<>(1 + pattern.args.size());
            exps.add(matchesConstructor(path, pattern.name));

            int i = 0;
            for (Pattern p : pattern.args) 
                exps.add(p.accept(this, path.extend(i++)));

            return CoreExpression.and(exps);
        }

        private CoreExpression matchesConstructor(PatternPath path, String name) {
            return new CoreEqualConstantExpression(name, new CoreExtractTagExpression(matchedObject, path));
        }

        @Override
        public CoreExpression visit(LiteralPattern pattern, PatternPath path) {
            return new CoreEqualConstantExpression(pattern.value, new CoreExtractExpression(matchedObject, path));
        }
    }
}
