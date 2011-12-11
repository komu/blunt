package fi.evident.dojolisp.eval;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

final class Environments {
    final StaticEnvironment staticEnvironment;
    final Environment runtimeEnvironment;

    Environments(StaticEnvironment staticEnvironment, Environment runtimeEnvironment) {
        this.staticEnvironment = requireNonNull(staticEnvironment);
        this.runtimeEnvironment = requireNonNull(runtimeEnvironment);
    }
}
