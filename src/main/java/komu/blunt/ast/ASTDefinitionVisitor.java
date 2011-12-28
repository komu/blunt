package komu.blunt.ast;

public interface ASTDefinitionVisitor<C,R> {
    R visit(ASTValueDefinition definition, C ctx);
    R visit(ASTDataDefinition definition, C ctx);
}
