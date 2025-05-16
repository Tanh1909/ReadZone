package vn.tnteco.common.data.constant;

import lombok.AllArgsConstructor;
import org.jooq.DatePart;
import vn.tnteco.common.utils.TimeUtils;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public enum FrequencyEnum {

    NONE("none", ""),
    MONTH("month", TimeUtils.MONTH_PATTERN),
    QUARTER("quarter", TimeUtils.QUARTER_PATTERN),
    DAY("day", TimeUtils.DATE_PATTERN),
    HOUR("hour", TimeUtils.DATE_TIME_PATTERN),
    DAY_OF_MONTH("day-of-month", TimeUtils.DATE_PATTERN);


    private final String value;
    private final String pattern;

    public String value() {
        return value;
    }

    public String pattern() {
        return pattern;
    }

    private static final Map<String, FrequencyEnum> mappingValue = new HashMap<>();

    static {
        for (FrequencyEnum frequencyEnum : FrequencyEnum.values()) {
            mappingValue.put(frequencyEnum.value, frequencyEnum);
        }
    }

    public static FrequencyEnum fromValue(String value) {
        return mappingValue.getOrDefault(value, NONE);
    }

    public DatePart getDatePart() {
        return switch (this) {
            case HOUR -> DatePart.HOUR;
            case DAY, DAY_OF_MONTH -> DatePart.DAY;
            case MONTH -> DatePart.MONTH;
            default -> null;
        };
    }
}
