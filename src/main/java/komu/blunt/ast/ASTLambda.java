package komu.blunt.ast;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;
import static komu.blunt.types.Type.functionType;

import java.util.List;

import komu.blunt.core.CoreExpression;
import komu.blunt.core.CoreLambdaExpression;
import komu.blunt.eval.StaticEnvironment;
import komu.blunt.objects.Symbol;
import komu.blunt.types.Assumptions;
import komu.blunt.types.ClassEnv;
import komu.blunt.types.Kind;
import komu.blunt.types.Type;
import komu.blunt.types.TypeCheckResult;
import komu.blunt.types.TypeChecker;
import komu.blunt.types.TypeVariable;

public final class ASTLambda extends ASTExpression {
    public final List<Symbol> arguments;
    public final ASTExpression body;

    public ASTLambda(List<Symbol> arguments, ASTExpression body) {
        if (arguments.isEmpty()) throw new IllegalArgumentException("no arguments for lambda");

        this.arguments = checkNotNull(arguments);
        this.body = checkNotNull(body);
    }

    public ASTLambda(Symbol argument, ASTExpression body) {
        this.arguments = asList(checkNotNull(argument));
        this.body = checkNotNull(body);
    }
    
    @Override
    public CoreExpression analyze(StaticEnvironment env) {
        if (arguments.size() == 1) {
            Symbol arg = arguments.get(0);
            StaticEnvironment newEnv = env.extend(arg);
            return new CoreLambdaExpression(arg, body.analyze(newEnv));
        } else {
            return rewrite().analyze(env);
        }
    }

    @Override
    public TypeCheckResult<Type> typeCheck(ClassEnv ce, TypeChecker tc, Assumptions as) {
        if (arguments.size() == 1) {
            Symbol arg = arguments.get(0);

            TypeVariable argumentType = tc.newTVar(Kind.STAR);

            Assumptions as2 = as.extend(arg, argumentType.toScheme());

            TypeCheckResult<Type> result = body.typeCheck(ce, tc, as2);

            return new TypeCheckResult<Type>(result.predicates, functionType(argumentType, result.value));
        } else {
            return rewrite().typeCheck(ce, tc, as);
        }
    }

    private ASTLambda rewrite() {
        return new ASTLambda(arguments.get(0), new ASTLambda(arguments.subList(1, arguments.size()), body));
    }

    @Override
    public String toString() {
        return "(lambda " + arguments + " " + body + ")";
    }
}
