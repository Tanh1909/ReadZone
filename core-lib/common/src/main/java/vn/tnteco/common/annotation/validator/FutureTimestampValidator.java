package vn.tnteco.common.annotation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import vn.tnteco.common.annotation.FutureTimestamp;

import java.time.Instant;

public class FutureTimestampValidator implements ConstraintValidator<FutureTimestamp, Long> {

    @Override
    public boolean isValid(Long value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null) return true;
        Instant input = Instant.ofEpochMilli(value);
        return input.isAfter(Instant.now());
    }

}
