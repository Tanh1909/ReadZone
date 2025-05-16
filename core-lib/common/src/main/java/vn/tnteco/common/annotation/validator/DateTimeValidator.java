package vn.tnteco.common.annotation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.ObjectUtils;
import vn.tnteco.common.annotation.ValidDateTime;
import vn.tnteco.common.utils.TimeUtils;

public class DateTimeValidator implements ConstraintValidator<ValidDateTime, String> {

    private String pattern;

    private boolean isEmpty;

    @Override
    public void initialize(ValidDateTime constraintAnnotation) {
        this.pattern = constraintAnnotation.pattern();
        this.isEmpty = constraintAnnotation.isEmpty();
    }

    @Override
    public boolean isValid(String dateTime, ConstraintValidatorContext constraintValidatorContext) {
        if (isEmpty && ObjectUtils.isEmpty(dateTime)) {
            return true;
        }
        try {
            return !ObjectUtils.isEmpty(dateTime) && TimeUtils.parseToLocalDateTime(dateTime, pattern) != null;
        } catch (Exception ex) {
            return false;
        }
    }
}