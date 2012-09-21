package komu.blunt.types.checker

import kotlin.util.*
import komu.blunt.ast.AST
import komu.blunt.ast.ASTExpression
import komu.blunt.ast.ASTValueDefinition
import komu.blunt.ast.BindGroup
import komu.blunt.eval.TypeCheckException
import komu.blunt.types.*
import komu.blunt.types.patterns.Pattern

import java.util.ArrayList

import komu.blunt.types.quantifyAll
import komu.blunt.types.instantiate
import komu.blunt.types.typeVariable

class TypeChecker(val classEnv: ClassEnv, private val dataTypes: DataTypeDefinitions) {

    private var typeSequence = 0
    private val expressionVisitor = ExpressionTypeCheckVisitor(this)
    private val bindingTypeChecker = BindingTypeChecker(this)
    private val patternTypeChecker = PatternTypeChecker(this)
    private var substitution = Substitutions.empty()

    class object {

        fun typeCheck(exp: ASTExpression, classEnv: ClassEnv, dataTypes: DataTypeDefinitions, ass: Assumptions): Qualified<Type> {
            val checker = TypeChecker(classEnv, dataTypes)
            return checker.normalize(checker.typeCheck(exp, ass))
        }

        fun typeCheck(exp: ASTValueDefinition, classEnv: ClassEnv, dataTypes: DataTypeDefinitions, ass: Assumptions): Scheme {
            val checker = TypeChecker(classEnv, dataTypes)

            return quantifyAll(checker.normalize(checker.typeCheck(exp, ass)))
        }
    }

    private fun normalize(result: TypeCheckResult<Type>): Qualified<Type> {
        val ps = classEnv.reduce(applySubstitution(result.predicates))
        return applySubstitution(Qualified(ps, result.value))
    }

    fun typeCheck(exp: ASTExpression, ass: Assumptions): TypeCheckResult<Type> =
        expressionVisitor.typeCheck(exp, ass)

    fun typeCheckBindGroup(bindGroup: BindGroup, ass: Assumptions): TypeCheckResult<Assumptions> =
        bindingTypeChecker.typeCheckBindGroup(bindGroup, ass)

    fun typeCheck(define: ASTValueDefinition, ass: Assumptions): TypeCheckResult<Type> {
        val let = AST.letRec(define.name, define.value, AST.variable(define.name))
        return typeCheck(let, ass)
    }

    fun typeCheck(pattern: Pattern): PatternTypeCheckResult<Type> =
        patternTypeChecker.typeCheck(pattern)

    fun freshInstance(scheme: Scheme): Qualified<Type> =
        instantiate(scheme.kinds.map { newTVar(it) }, scheme.`type`)

    fun newTVar(): TypeVariable =
        newTVar(Kind.STAR)

    fun newTVar(kind: Kind): TypeVariable =
        typeVariable(typeName(typeSequence++), kind)

    fun newTVars(size: Int): List<Type> {
        val types = ArrayList<Type>(size)
        for (val i in 1..size)
            types.add(newTVar())
        return types
    }

    private fun typeName(index: Int): String =
        if (index < 5)
            ('a' + index).toChar().toString()
        else
            "t" + (index-5)

    fun unify(t1: Type, t2: Type) {
        try {
            val u = Unifier.mgu(t1.apply(substitution), t2.apply(substitution))
            substitution = u.compose(substitution)
        } catch (e: UnificationException) {
            throw TypeCheckException(e.toString())
        }
    }

    fun applySubstitution<T : Types<T>>(t: T): T =
      t.apply(substitution)

    fun applySubstitution<T : Types<T>>(t: Qualified<T>): Qualified<T> =
      t.apply(substitution)

    // TODO
    fun applySubstitution(t: Assumptions): Assumptions =
      t.apply(substitution)

    fun applySubstitution<T : Types<T>>(ts: Collection<T>): List<T> =
        ts.map { it.apply(substitution) }

    fun findConstructor(name: String): ConstructorDefinition =
        dataTypes.findConstructor(name)
}
