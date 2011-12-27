package komu.blunt.ast;

import komu.blunt.analyzer.AnalyzingVisitor;
import komu.blunt.analyzer.StaticEnvironment;
import komu.blunt.analyzer.VariableReference;
import komu.blunt.core.CoreDefineExpression;
import komu.blunt.core.CoreExpression;
import komu.blunt.objects.Symbol;
import komu.blunt.types.Type;
import komu.blunt.types.checker.TypeCheckResult;
import komu.blunt.types.checker.TypeCheckingContext;
import komu.blunt.types.checker.TypeCheckingVisitor;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ASTDefine {

    public final Symbol name;
    public final ASTExpression value;

    public ASTDefine(Symbol name, ASTExpression value) {
        this.name = checkNotNull(name);
        this.value = checkNotNull(value);
    }

    public CoreExpression analyze(StaticEnvironment rootEnv) {
        AnalyzingVisitor analyzer = new AnalyzingVisitor();
        VariableReference var = rootEnv.define(name);

        return new CoreDefineExpression(analyzer.analyze(value, rootEnv), var);
    }

    public TypeCheckResult<Type> typeCheck(TypeCheckingContext ctx) {
        TypeCheckingVisitor checker = new TypeCheckingVisitor();
        return checker.typeCheck(new ASTLetRec(name, value, new ASTVariable(name)), ctx);
    }

    @Override
    public String toString() {
        return "(define " + name + " " + value + ")";
    }
}
