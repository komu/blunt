A simple compiler/interpreter.

The main flow:

           String
             |
             |  parser.Lexer
             |
             v
     parser.Token sequence
             |
             |  parser.Parser
             |
             v
      ast.ASTExpression
             |
             |  types.checker.TypeChecker
             |
             v
      ast.ASTExpression
             |
             |  analyzer.Analyzer
             |
             v
      core.CoreExpression
             |
             |  CoreExpression.assemble
             |
             v
       asm.Instructions
             |
             |  asm.VM.run
             o

Things to do:

- non-generic type parameters for constructors
- pattern matching against data types
    - rewrite if to pattern match
- explicit type definitions
- type aliases
- type classes
- newtype declarations
- type declarations for stdlib

IDEA configuration
------------------

Most project settings are in version control, but unfortunately we have to prepare a new custom
SDK for Blunt, since we have external annotations for the SDK. When first checking out the
project, go to "File > Project Structure > Platform Settings > SDKs" and create a new SDK named
"1.7-blunt", which points an installation of Java 1.7 SDK. Then go to "Annotions"-page of the
newly created SDK and add a reference to Blunt's "annotations-"directory. After this, make sure
that the "Project Settings > Project > Project SDK" points to "1.7-blunt".
