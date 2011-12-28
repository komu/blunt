package komu.blunt.ast;

import com.google.common.collect.ImmutableList;
import komu.blunt.types.ConstructorDefinition;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ASTDataDefinition extends ASTDefinition {

    private final String name;
    public final ImmutableList<ConstructorDefinition> constructors;

    public ASTDataDefinition(String name, ImmutableList<ConstructorDefinition> constructors) {
        this.name = checkNotNull(name);
        this.constructors = checkNotNull(constructors);
    }

    @Override
    public <C, R> R accept(ASTDefinitionVisitor<C, R> visitor, C ctx) {
        return visitor.visit(this, ctx);
    }

    @Override
    public String toString() {
        return "data " + name + " = " + constructors;
    }
}
