package komu.blunt.ast

import komu.blunt.objects.Symbol
import komu.blunt.types.ConstructorDefinition
import komu.blunt.types.Type

sealed class ASTDefinition {

    class Value(val name: Symbol, val value: ASTExpression) : ASTDefinition() {
        override fun toString() = "(define $name $value)"
    }

    class Data(val name: String, val type: Type, val constructors: List<ConstructorDefinition>, val derivedClasses: List<String>) : ASTDefinition() {
        override fun toString() = "data $name = $constructors"
    }
}
