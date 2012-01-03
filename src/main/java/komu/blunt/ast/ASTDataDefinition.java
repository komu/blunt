package komu.blunt.ast;

import com.google.common.collect.ImmutableList;
import komu.blunt.types.ConstructorDefinition;
import komu.blunt.types.Type;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ASTDataDefinition extends ASTDefinition {

    private final String name;
    public final Type type;
    public final ImmutableList<ConstructorDefinition> constructors;
    public final ImmutableList<String> derivedClasses;

    ASTDataDefinition(String name, Type type, ImmutableList<ConstructorDefinition> constructors, ImmutableList<String> derivedClasses) {
        this.name = checkNotNull(name);
        this.type = checkNotNull(type);
        this.constructors = checkNotNull(constructors);
        this.derivedClasses = checkNotNull(derivedClasses);
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
