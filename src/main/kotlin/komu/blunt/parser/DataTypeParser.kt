package komu.blunt.parser

import com.google.common.collect.ImmutableList
import komu.blunt.ast.AST
import komu.blunt.ast.ASTDataDefinition
import komu.blunt.types.*

import java.util.ArrayList
import java.util.List

import com.google.common.base.Preconditions.checkNotNull
import komu.blunt.parser.TokenType.*
import komu.blunt.types.quantify

private class DataTypeParser(val lexer: Lexer, val typeParser: TypeParser) {

    // data <type> <var>* = <constructor>+
    fun parseDataDefinition(): ASTDataDefinition  {
        lexer.expectIndentStartToken(DATA)

        val builder = DataTypeBuilder(lexer.readTokenValue(TYPE_OR_CTOR_NAME))

        while (!lexer.readMatchingToken(ASSIGN))
            builder.addVariable(typeParser.parseTypeVariable())

        do {
            parseConstructor(builder)
        } while (lexer.readMatchingToken(OR))

        if (lexer.readMatchingToken(DERIVING)) {
            lexer.expectToken(LPAREN)
            do {
                builder.addAutomaticallyDerivedClass(lexer.readTokenValue(TYPE_OR_CTOR_NAME))
            } while (lexer.readMatchingToken(COMMA))
            lexer.expectToken(RPAREN)
        }

        lexer.expectToken(END)

        return builder.build()
    }

    private fun parseConstructor(builder: DataTypeBuilder): Unit {
        val constructorName = lexer.readTokenValue(TYPE_OR_CTOR_NAME)

        val args = ArrayList<Type>()
        while (!lexer.nextTokenIs(OR) && !lexer.nextTokenIs(END) && !lexer.nextTokenIs(DERIVING))
            args.add(typeParser.parseTypePrimitive())

        builder.addConstructor(constructorName, args)
    }

    private class DataTypeBuilder(val typeName: String) {

        private val vars = ArrayList<TypeVariable>()
        private val constructors = ArrayList<ConstructorDefinition>()
        private val derivedClasses = ArrayList<String>()
        private var constructorIndex = 0

        public fun addVariable(variable: TypeVariable) {
            vars.add(variable)
        }

        public fun addConstructor(constructorName: String, args: List<Type>) {
            val scheme = quantify(vars, Qualified(Type.function(args, getType())))
            constructors.add(ConstructorDefinition(constructorIndex++, constructorName, scheme, args.size()))
        }

        private fun getType() =
            Type.generic(typeName, vars)

        public fun addAutomaticallyDerivedClass(className: String) {
            derivedClasses.add(className)
        }

        public fun build(): ASTDataDefinition =
            AST.data(typeName, getType(), ImmutableList.copyOf(constructors).sure(), ImmutableList.copyOf(derivedClasses).sure())
    }
}
