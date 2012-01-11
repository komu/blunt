package komu.blunt.ast;

import komu.blunt.objects.Symbol;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ImplicitBinding {
    public final Symbol name;
    public final ASTExpression expr;

    public ImplicitBinding(Symbol name, ASTExpression expr) {
        this.name = checkNotNull(name);
        this.expr = checkNotNull(expr);
    }

    @Override
    public String toString() {
        return "[" + name + " " + expr + "]";
    }

    public static List<Symbol> bindingNames(List<ImplicitBinding> bs) {
        List<Symbol> names = new ArrayList<>(bs.size());
        for (ImplicitBinding b : bs)
            names.add(b.name);
        return names;
    }
}
