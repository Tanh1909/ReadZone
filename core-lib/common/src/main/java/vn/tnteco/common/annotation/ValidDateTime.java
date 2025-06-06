package vn.tnteco.common.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import vn.tnteco.common.annotation.validator.DateTimeValidator;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Constraint(validatedBy = DateTimeValidator.class)
public @interface ValidDateTime {

    String message() default "Invalid date time";

    String pattern() default "";

    boolean isEmpty() default false;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}