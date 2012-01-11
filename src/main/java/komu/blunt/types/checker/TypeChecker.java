package komu.blunt.types.checker;

import komu.blunt.ast.AST;
import komu.blunt.ast.ASTExpression;
import komu.blunt.ast.ASTValueDefinition;
import komu.blunt.ast.BindGroup;
import komu.blunt.eval.TypeCheckException;
import komu.blunt.types.*;
import komu.blunt.types.patterns.Pattern;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static komu.blunt.types.Qualified.quantifyAll;
import static komu.blunt.types.Type.typeVariable;
import static komu.blunt.types.checker.Unifier.mgu;

public final class TypeChecker {

    public final ClassEnv classEnv;
    private final DataTypeDefinitions dataTypes;
    private int typeSequence = 0;
    private final ExpressionTypeCheckVisitor expressionVisitor = new ExpressionTypeCheckVisitor(this);
    private final BindingTypeChecker bindingTypeChecker = new BindingTypeChecker(this);
    private final PatternTypeChecker patternTypeChecker = new PatternTypeChecker(this);
    private Substitution substitution = Substitution.empty();

    private TypeChecker(ClassEnv classEnv, DataTypeDefinitions dataTypes) {
        this.classEnv = checkNotNull(classEnv);
        this.dataTypes = checkNotNull(dataTypes);
    }

    public static Qualified<Type> typeCheck(ASTExpression exp, ClassEnv classEnv, DataTypeDefinitions dataTypes, Assumptions as) {
        TypeChecker checker = new TypeChecker(classEnv, dataTypes);

        return checker.normalize(checker.typeCheck(exp, as));
    }

    public static Scheme typeCheck(ASTValueDefinition exp, ClassEnv classEnv, DataTypeDefinitions dataTypes, Assumptions as) {
        TypeChecker checker = new TypeChecker(classEnv, dataTypes);

        return quantifyAll(checker.normalize(checker.typeCheck(exp, as)));
    }

    private Qualified<Type> normalize(TypeCheckResult<Type> result) {
        List<Predicate> ps = classEnv.reduce(applySubstitution(result.predicates));
        return applySubstitution(new Qualified<>(ps, result.value));
    }

    TypeCheckResult<Type> typeCheck(ASTExpression exp, Assumptions as) {
        return exp.accept(expressionVisitor, as);
    }

    TypeCheckResult<Assumptions> typeCheckBindGroup(BindGroup bindGroup, Assumptions as) {
        return bindingTypeChecker.typeCheckBindGroup(bindGroup, as);
    }

    TypeCheckResult<Type> typeCheck(ASTValueDefinition define, Assumptions as) {
        ASTExpression let = AST.letRec(define.name, define.value, AST.variable(define.name));
        return typeCheck(let, as);
    }

    PatternTypeCheckResult<Type> typeCheck(Pattern pattern) {
        return pattern.accept(patternTypeChecker, null);
    }

    Qualified<Type> freshInstance(Scheme scheme) {
        List<TypeVariable> ts = new ArrayList<>(scheme.kinds.size());
        for (Kind kind : scheme.kinds)
            ts.add(newTVar(kind));

        return Qualified.instantiate(ts, scheme.type);
    }

    TypeVariable newTVar() {
        return newTVar(Kind.STAR);
    }

    TypeVariable newTVar(Kind kind) {
        return typeVariable(typeName(typeSequence++), kind);
    }

    List<Type> newTVars(int size) {
        List<Type> types = new ArrayList<>(size);
        for (int i = 0; i < size; i++)
            types.add(newTVar());
        return types;
    }

    private static String typeName(int index) {
        if (index < 5) {
            return String.valueOf((char) ('a' + index));
        } else {
            return "t" + (index-5);
        }
    }

    void unify(Type t1, Type t2) {
        try {
            Substitution u = mgu(t1.apply(substitution), t2.apply(substitution));
            substitution = u.compose(substitution);
        } catch (UnificationException e) {
            throw new TypeCheckException(e);
        }
    }

    <T extends Types<T>> T applySubstitution(T t) {
        return t.apply(substitution);
    }

    <T extends Types<T>> List<T> applySubstitution(Collection<T> ts) {
        return TypeUtils.applySubstitution(substitution, ts);
    }

    public ConstructorDefinition findConstructor(String name) {
        return dataTypes.findConstructor(name);
    }
}
