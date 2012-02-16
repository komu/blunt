package komu.blunt.types.checker

import komu.blunt.ast.AST
import komu.blunt.ast.ASTExpression
import komu.blunt.ast.ASTValueDefinition
import komu.blunt.ast.BindGroup
import komu.blunt.eval.TypeCheckException
import komu.blunt.types.*
import komu.blunt.types.patterns.Pattern

import java.util.ArrayList
import java.util.Collection
import java.util.List

import komu.blunt.types.Qualified.quantifyAll
import komu.blunt.types.Type.typeVariable
import komu.blunt.types.checker.Unifier.mgu

class TypeChecker(private val classEnv: ClassEnv, private val dataTypes: DataTypeDefinitions) {

    private var typeSequence = 0
    private val expressionVisitor = ExpressionTypeCheckVisitor(this)
    private val bindingTypeChecker = BindingTypeChecker(this)
    private val patternTypeChecker = PatternTypeChecker(this)
    private var substitution = Substitution.empty().sure()

    class object {

        fun typeCheck(exp: ASTExpression, classEnv: ClassEnv, dataTypes: DataTypeDefinitions, ass: Assumptions): Qualified<Type?> {
            val checker = TypeChecker(classEnv, dataTypes)
            return checker.normalize(checker.typeCheck(exp, ass))
        }

        fun typeCheck(exp: ASTValueDefinition, classEnv: ClassEnv, dataTypes: DataTypeDefinitions, ass: Assumptions): Scheme {
            val checker = TypeChecker(classEnv, dataTypes)

            return quantifyAll(checker.normalize(checker.typeCheck(exp, ass))).sure()
        }
    }

    private fun normalize(result: TypeCheckResult<Type?>): Qualified<Type?> {
        val ps = classEnv.reduce(applySubstitution(result.predicates))
        return applySubstitution(Qualified(ps, result.value))
    }

    fun typeCheck(exp: ASTExpression, ass: Assumptions): TypeCheckResult<Type?> =
        exp.accept(expressionVisitor, ass).sure()

    fun typeCheckBindGroup(bindGroup: BindGroup, ass: Assumptions): TypeCheckResult<Assumptions?> =
        bindingTypeChecker.typeCheckBindGroup(bindGroup, ass)

    fun typeCheck(define: ASTValueDefinition, ass: Assumptions): TypeCheckResult<Type?> {
        val let = AST.letRec(define.name, define.value, AST.variable(define.name)).sure()
        return typeCheck(let, ass)
    }

    fun typeCheck(pattern: Pattern): PatternTypeCheckResult<Type?> =
        pattern.accept(patternTypeChecker, null)

    fun freshInstance(scheme: Scheme): Qualified<Type?> {
        val ts = ArrayList<TypeVariable?>(scheme.kinds.size().sure())
        for (val kind in scheme.kinds)
            ts.add(newTVar(kind))

        return Qualified.instantiate(ts, scheme.`type`).sure()
    }

    fun newTVar(): TypeVariable =
        newTVar(Kind.STAR)
    }

    fun newTVar(kind: Kind): TypeVariable =
        typeVariable(typeName(typeSequence++), kind)

//
//    List<Type> newTVars(int size) {
//        List<Type> types = new ArrayList<>(size);
//        for (int i = 0; i < size; i++)
//            types.add(newTVar());
//        return types;
//    }
//
//    private static String typeName(int index) {
//        if (index < 5) {
//            return String.valueOf((char) ('a' + index));
//        } else {
//            return "t" + (index-5);
//        }
//    }
//
//    void unify(Type t1, Type t2) {
//        try {
//            Substitution u = mgu(t1.apply(substitution), t2.apply(substitution));
//            substitution = u.compose(substitution);
//        } catch (UnificationException e) {
//            throw new TypeCheckException(e);
//        }
//    }
//
//    <T extends Types<T>> T applySubstitution(T t) {
//        return t.apply(substitution);
//    }
//
//    <T extends Types<T>> List<T> applySubstitution(Collection<T> ts) {
//        return TypeUtils.applySubstitution(substitution, ts);
//    }
//
    fun findConstructor(name: String): ConstructorDefinition =
        dataTypes.findConstructor(name)
}
