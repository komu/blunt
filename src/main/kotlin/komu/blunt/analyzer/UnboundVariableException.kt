package komu.blunt.analyzer

import komu.blunt.objects.Symbol

class UnboundVariableException(variable: Symbol) : AnalyzationException("unbound variable '$variable'")

