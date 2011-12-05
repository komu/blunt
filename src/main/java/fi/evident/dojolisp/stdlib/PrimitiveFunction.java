package fi.evident.dojolisp.stdlib;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface PrimitiveFunction {
    String value();
}
