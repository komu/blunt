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
  - visitor for core expressions
