package vn.tnteco.common.data.mapper;

import org.mapstruct.Mapper;
import vn.tnteco.common.utils.TimeUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Mapper(componentModel = "spring")
public interface TimeMapper {

    default LocalDateTime mapToLocalDateTime(Long input) {
        return TimeUtils.epochMilliToLocalDateTime(input);
    }

    default LocalTime mapToLocalTime(Integer input) {
        if (input == null || input < 0) return null;
        try {
            return LocalTime.ofSecondOfDay(input);
        } catch (Exception e) {
            return null;
        }
    }

    default LocalDateTime mapToLocalDateTime(String input) {
        return TimeUtils.parseToLocalDateTime(input);
    }

    default Long mapToLong(LocalDateTime time) {
        return TimeUtils.localDateTimeToEpochMilli(time);
    }

    default Long mapToLong(LocalDate time) {
        return TimeUtils.localDateToEpochMilli(time);
    }

    default Integer mapToLong(LocalTime time) {
        if (time == null) return null;
        return time.toSecondOfDay();
    }

}
