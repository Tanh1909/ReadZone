package vn.tnteco.common.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import vn.tnteco.common.annotation.validator.EnumValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = EnumValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEnum {

    Class<? extends Enum<?>> enumClass();

    String enumField() default "";

    String message() default "Invalid value. Must be any of enumClass";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}