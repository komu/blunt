package komu.blunt.stdlib;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface LibraryFunction {
    String value();
    String type() default "";
}
