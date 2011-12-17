package fi.evident.dojolisp.eval;

import fi.evident.dojolisp.types.TypeEnvironment;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

final class Environments {
    final StaticEnvironment staticEnvironment;
    final TypeEnvironment typeEnvironment;
    final RootEnvironment runtimeEnvironment;

    Environments(StaticEnvironment staticEnvironment,
                 TypeEnvironment typeEnvironment,
                 RootEnvironment runtimeEnvironment) {
        this.staticEnvironment = requireNonNull(staticEnvironment);
        this.typeEnvironment = requireNonNull(typeEnvironment);
        this.runtimeEnvironment = requireNonNull(runtimeEnvironment);
    }
}
