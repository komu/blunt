package komu.blunt.core;

import komu.blunt.asm.Instructions;
import komu.blunt.asm.Linkage;
import komu.blunt.asm.Register;
import komu.blunt.types.Type;
import komu.blunt.types.TypeEnvironment;

import java.util.ArrayList;
import java.util.List;

public abstract class CoreExpression {
    public abstract Type typeCheck(TypeEnvironment env);
    public abstract void assemble(Instructions instructions, Register target, Linkage linkage);
    
    protected static List<Type> typeCheckAll(List<CoreExpression> exps, TypeEnvironment env) {
        List<Type> types = new ArrayList<Type>(exps.size());
        
        for (CoreExpression exp : exps)
            types.add(exp.typeCheck(env));

        return types;
    }
}
