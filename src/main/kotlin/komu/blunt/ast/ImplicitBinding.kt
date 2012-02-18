package komu.blunt.ast

import komu.blunt.objects.Symbol

import java.util.ArrayList
import java.util.List;

class ImplicitBinding(val name: Symbol, val expr: ASTExpression) {

    fun toString() = "[$name $expr]"
    fun simplify() = ImplicitBinding(name, expr.simplify().sure())

    class object {
        fun bindingNames(bs: List<ImplicitBinding?>): List<Symbol?> {
            val names = ArrayList<Symbol?>(bs.sure().size())
            for (val b in bs)
                names.add(b?.name)
            return names
        }
    }
}

