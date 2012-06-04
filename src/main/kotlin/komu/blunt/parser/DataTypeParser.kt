package komu.blunt.parser

import kotlin.util.*
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
        lexer.expectIndentStartToken(TokenType.DATA)

        val builder = DataTypeBuilder(lexer.readTokenValue(TokenType.TYPE_OR_CTOR_NAME))

        while (!lexer.readMatchingToken(TokenType.ASSIGN))
            builder.addVariable(typeParser.parseTypeVariable())

        do {
            parseConstructor(builder)
        } while (lexer.readMatchingToken(TokenType.OR))

        if (lexer.readMatchingToken(TokenType.DERIVING)) {
            lexer.expectToken(TokenType.LPAREN)
            do {
                builder.addAutomaticallyDerivedClass(lexer.readTokenValue(TokenType.TYPE_OR_CTOR_NAME))
            } while (lexer.readMatchingToken(TokenType.COMMA))
            lexer.expectToken(TokenType.RPAREN)
        }

        lexer.expectToken(TokenType.END)

        return builder.build()
    }

    private fun parseConstructor(builder: DataTypeBuilder): Unit {
        val constructorName = lexer.readTokenValue(TokenType.TYPE_OR_CTOR_NAME)

        val args = ArrayList<Type>()
        while (!lexer.nextTokenIs(TokenType.OR) && !lexer.nextTokenIs(TokenType.END) && !lexer.nextTokenIs(TokenType.DERIVING))
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
            val scheme = quantify(vars, Qualified<Type>(functionType(args, getType())))
            constructors.add(ConstructorDefinition(constructorIndex++, constructorName, scheme, args.size))
        }

        private fun getType() =
            genericType(typeName, vars)

        public fun addAutomaticallyDerivedClass(className: String) {
            derivedClasses.add(className)
        }

        public fun build(): ASTDataDefinition =
            AST.data(typeName, getType(), ImmutableList.copyOf(constructors).sure(), ImmutableList.copyOf(derivedClasses).sure())
    }
}
