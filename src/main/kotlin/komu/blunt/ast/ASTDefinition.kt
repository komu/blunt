package komu.blunt.ast

import com.google.common.collect.ImmutableList;
import komu.blunt.objects.Symbol
import komu.blunt.types.ConstructorDefinition;
import komu.blunt.types.Type;

abstract class ASTDefinition

class ASTValueDefinition(val name: Symbol, val value: ASTExpression) : ASTDefinition() {
    fun toString() = "(define $name $value)"
}

class ASTDataDefinition(val name: String,
                        val typ: Type,
                        val constructors: ImmutableList<ConstructorDefinition>,
                        val derivedClasses: ImmutableList<String>) : ASTDefinition() {

    fun toString() = "data $name = $constructors"
}
