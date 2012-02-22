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

class TypeChecker(val classEnv: ClassEnv, private val dataTypes: DataTypeDefinitions) {

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

    fun typeCheck(exp: ASTExpression, ass: Assumptions): TypeCheckResult<Type> =
        expressionVisitor.typeCheck(exp, ass)

    fun typeCheckBindGroup(bindGroup: BindGroup, ass: Assumptions): TypeCheckResult<Assumptions> =
        bindingTypeChecker.typeCheckBindGroup(bindGroup, ass)

    fun typeCheck(define: ASTValueDefinition, ass: Assumptions): TypeCheckResult<Type> {
        val let = AST.letRec(define.name.sure(), define.value.sure(), AST.variable(define.name.sure()))
        return typeCheck(let, ass)
    }

    fun typeCheck(pattern: Pattern): PatternTypeCheckResult<Type> =
        patternTypeChecker.typeCheck(pattern)

    fun freshInstance(scheme: Scheme): Qualified<Type?> {
        val ts = ArrayList<TypeVariable?>(scheme.kinds?.size().sure())
        for (val kind in scheme.kinds)
            ts.add(newTVar(kind.sure()))

        return Qualified.instantiate(ts, scheme.`type`).sure()
    }

    fun newTVar(): TypeVariable =
        newTVar(Kind.STAR.sure())

    fun newTVar(kind: Kind): TypeVariable =
        typeVariable(typeName(typeSequence++), kind).sure()

    fun newTVars(size: Int): List<Type?> {
        val types = ArrayList<Type?>(size)
        for (val i in 0..size-1)
            types.add(newTVar())
        return types
    }

    private fun typeName(index: Int): String =
        if (index < 5)
            String.valueOf(('a' + index).chr).sure()
        else
            "t" + (index-5)

    fun unify(t1: Type, t2: Type) {
        try {
            val u = mgu(t1.apply(substitution), t2.apply(substitution)).sure()
            substitution = u.compose(substitution).sure()
        } catch (e: UnificationException) {
            throw TypeCheckException(e)
        }
    }

    fun applySubstitution<T : Types<T?>>(t: T): T =
      t.apply(substitution).sure()

    // TODO
    fun applySubstitution(t: Assumptions): Assumptions =
      t.apply(substitution).sure()

    fun applySubstitution<T : Types<T?>>(ts: Collection<T?>): List<T?> {
        val result = ArrayList<T?>(ts.size())

        for (val t in ts)
            result.add(t?.apply(substitution))

        return result
    }

    fun findConstructor(name: String): ConstructorDefinition =
        dataTypes.findConstructor(name).sure()
}
