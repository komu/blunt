package komu.blunt.types.checker

import komu.blunt.ast.ASTDefinition
import komu.blunt.ast.ASTExpression
import komu.blunt.ast.BindGroup
import komu.blunt.eval.TypeCheckException
import komu.blunt.types.*
import komu.blunt.types.patterns.Pattern

class TypeChecker(val classEnv: ClassEnv, private val dataTypes: DataTypeDefinitions) {

    private var typeSequence = 0
    private val expressionVisitor = ExpressionTypeCheckVisitor(this)
    private val bindingTypeChecker = BindingTypeChecker(this)
    private val patternTypeChecker = PatternTypeChecker(this)
    private var substitution = Substitution.empty

    companion object {

        fun typeCheck(exp: ASTExpression, classEnv: ClassEnv, dataTypes: DataTypeDefinitions, ass: Assumptions): Qualified<Type> {
            val checker = TypeChecker(classEnv, dataTypes)
            return checker.normalize(checker.typeCheck(exp, ass))
        }

        fun typeCheck(exp: ASTDefinition.Value, classEnv: ClassEnv, dataTypes: DataTypeDefinitions, ass: Assumptions): Scheme {
            val checker = TypeChecker(classEnv, dataTypes)
            return checker.normalize(checker.typeCheck(exp, ass)).quantifyAll()
        }
    }

    private fun normalize(result: TypeCheckResult<Type>): Qualified<Type> {
        val ps = classEnv.reduce(applySubstitution(result.predicates))
        return Qualified(ps, result.value).apply(substitution)
    }

    fun typeCheck(exp: ASTExpression, ass: Assumptions): TypeCheckResult<Type> =
        expressionVisitor.typeCheck(exp, ass)

    fun typeCheckBindGroup(bindGroup: BindGroup, ass: Assumptions): TypeCheckResult<Assumptions> =
        bindingTypeChecker.typeCheckBindGroup(bindGroup, ass)

    fun typeCheck(define: ASTDefinition.Value, ass: Assumptions): TypeCheckResult<Type> =
        typeCheck(ASTExpression.LetRec(define.name, define.value, ASTExpression.Variable(define.name)), ass)

    fun typeCheck(pattern: Pattern): PatternTypeCheckResult<Type> =
        patternTypeChecker.typeCheck(pattern)

    fun freshInstance(scheme: Scheme): Qualified<Type> =
        scheme.type.instantiate(newTVars(scheme.kinds))

    fun newTVar(): Type.Var =
        newTVar(Kind.Star)

    fun newTVar(kind: Kind): Type.Var =
        Type.Var(typeName(typeSequence++), kind)

    fun newTVars(size: Int): List<Type.Var> =
        (1..size).map { newTVar() }

    fun newTVars(kinds: List<Kind>): List<Type.Var> =
        kinds.map { newTVar(it) }

    private fun typeName(index: Int): String =
        if (index < 5)
            ('a' + index).toChar().toString()
        else
            "t" + (index-5)

    fun unify(t1: Type, t2: Type) {
        val tt1 = t1.apply(substitution)
        val tt2 = t2.apply(substitution)
        try {
            val u = Unifier.mgu(tt1, tt2)
            substitution = u.compose(substitution)
        } catch (e: UnificationException) {
            throw TypeCheckException("$tt1 -- $tt2 -- $e")
        }
    }

    // TODO
    fun applySubstitution(t: Assumptions): Assumptions =
      t.apply(substitution)

    fun <T : Types<T>> applySubstitution(ts: Collection<T>): List<T> =
        ts.map { it.apply(substitution) }

    fun findConstructor(name: String): ConstructorDefinition =
        dataTypes.findConstructor(name)
}
