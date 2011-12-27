A simple compiler/interpreter.

The main flow:

       String
         |
         |  parser.Lexer
         |
         v
       tokens
         |
         |  parser.Parser
         |
         v
  ast.ASTExpression
         |
         |  types.types.TypeCheckingVisitor
         |
         v
  ast.ASTExpression
         |
         |  analyzer.AnalyzingVisitor
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
  - data type definitions
  - type aliases
  - type classes
  - newtype declarations
  - type declarations for stdlib
  - visitor for core expressions
