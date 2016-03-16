package komu.blunt.ast

import komu.blunt.objects.Symbol
import komu.blunt.types.ConstructorDefinition
import komu.blunt.types.Type

abstract class ASTDefinition

class ASTValueDefinition(val name: Symbol, val value: ASTExpression) : ASTDefinition() {
    override fun toString() = "(define $name $value)"
}

class ASTDataDefinition(val name: String,
                        val typ: Type,
                        val constructors: List<ConstructorDefinition>,
                        val derivedClasses: List<String>) : ASTDefinition() {

    override fun toString() = "data $name = $constructors"
}
