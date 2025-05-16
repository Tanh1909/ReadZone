package vn.tnteco.common.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelField {
    String name() default "";

    int position() default 0;
    String field() default "";

    boolean enabled() default true;

    String description() default "";
}
