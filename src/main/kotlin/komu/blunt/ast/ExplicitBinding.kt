package komu.blunt.ast

import komu.blunt.objects.Symbol
import komu.blunt.types.Scheme

class ExplicitBinding(val name: Symbol, val scheme: Scheme, value: ASTExpression)

