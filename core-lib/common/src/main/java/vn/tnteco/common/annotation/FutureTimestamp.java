package vn.tnteco.common.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import vn.tnteco.common.annotation.validator.FutureTimestampValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = FutureTimestampValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface FutureTimestamp {

    String message() default "time must be greater than present";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
