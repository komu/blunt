package komu.blunt.ast;

public abstract class ASTDefinition {
    public abstract <C,R> R accept(ASTDefinitionVisitor<C,R> visitor, C ctx);
}
