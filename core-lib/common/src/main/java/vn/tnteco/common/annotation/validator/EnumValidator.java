package vn.tnteco.common.annotation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;
import vn.tnteco.common.annotation.ValidEnum;
import vn.tnteco.common.utils.ReflectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class EnumValidator implements ConstraintValidator<ValidEnum, String> {

    private Class<? extends Enum<?>> enumClass;

    private String enumField;

    @Override
    public void initialize(ValidEnum annotation) {
        this.enumClass = annotation.enumClass();
        this.enumField = annotation.enumField();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;
        if (StringUtils.isEmpty(enumField)) {
            List<String> validValues = Arrays.stream(enumClass.getEnumConstants())
                    .map(Enum::name)
                    .toList();
            throwErrorIfOccurs(value, context, validValues);
            return validValues.stream()
                    .anyMatch(enumValue -> enumValue.equals(value));
        }
        List<String> validValues = Arrays.stream(enumClass.getEnumConstants())
                .map(enumConstant -> {
                    Object valueByFieldName = ReflectUtils.getValueByFieldName(enumConstant, enumField);
                    if (valueByFieldName != null) {
                        return valueByFieldName.toString();
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
        throwErrorIfOccurs(value, context, validValues);
        return validValues.stream()
                .anyMatch(value::equals);
    }

    private static void throwErrorIfOccurs(String value, ConstraintValidatorContext context, List<String> validValues) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("Invalid value '" + value + "'. Allowed values: " + validValues)
                .addConstraintViolation();
    }
}
