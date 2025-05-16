package vn.tnteco.common.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import vn.tnteco.common.data.constant.FrequencyEnum;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.stream.Stream;

import static java.time.ZonedDateTime.now;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.time.temporal.ChronoField.INSTANT_SECONDS;
import static java.time.temporal.TemporalAdjusters.*;


@Log4j2
@UtilityClass
public class TimeUtils {

    public static final String VIETNAM_DATE_TIME_PATTERN = "dd-MM-yyyy HH:mm:ss";
    public static final String VIETNAM_DATE_TIME_2_PATTERN = "dd-MM-yyyy HH:mm";
    public static final String ISO_DATE_TIME_UTC_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String ISO_DATE_TIME_UTC_2_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public static final String ISO_OFFSET_DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ssXXX";
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String TIME_DATE_PATTERN = "HH:mm:ss yyyy-MM-dd";
    public static final String DATE_TIME_ID_PATTERN = "yyyyMMddHHmmss";
    public static final String DATE_PATTERN = "dd-MM-yyyy";
    public static final String QUARTER_PATTERN = "Q-yyyy";
    public static final String MONTH_PATTERN = "MM-yyyy";

    public static Long getCurrentEpochMilli() {
        return new Timestamp(System.currentTimeMillis()).getTime();
    }

    public static Long getOneDayMini() {
        return 24 * 60 * 60 * 1000L;
    }

    public static Long getOneHourMini() {
        return 60 * 60 * 1000L;
    }

    public static Long getOneMonthMini() {
        return 30 * 24 * 60 * 60 * 1000L;
    }

    public static String getHour(Long time) {
        return new SimpleDateFormat("HH").format(new Date(time));
    }

    public static Integer getMonth(Long time) {
        return Integer.parseInt(new SimpleDateFormat("MM").format(new Date(time)));
    }

    public static Integer getYear(Long time) {
        return Integer.parseInt(new SimpleDateFormat("yyyy").format(new Date(time)));
    }

    public static int getThisYear() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.YEAR);
    }

    public static int getThisMonth() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.MONTH) + 1;
    }

    public static int getNumDayOfMonth(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(time));
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }


    public static Long getStartTimeOfDay() {
        return LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public static Long getEndTimeOfDay() {
        return LocalDate.now().atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public static LocalDateTime getStartTimeOfDayLDT() {
        return LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static Long getStartTimeOfDayFromTime(long time) {
        LocalDate localDate = epochMilliToLocalDate(time);
        if (localDate == null) return null;
        return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public static Long getEndTimeOfDayFromTime(Long time) {
        LocalDate localDate = epochMilliToLocalDate(time);
        if (localDate == null) return null;
        return localDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public static LocalDate getStartDateOfMonth() {
        return YearMonth.now().atDay(1);
    }

    public static LocalDate getEndDateOfMonth() {
        return YearMonth.now().atEndOfMonth();
    }

    public static LocalDate getStartDateOfYear() {
        return LocalDate.now().withDayOfYear(1);
    }

    public static LocalDateTime getStartDateOfYearLDT(Integer minusYears) {
        return LocalDateTime.now()
                .with(firstDayOfYear())
                .toLocalDate()
                .atTime(LocalTime.MIN)
                .minusYears(minusYears);
    }

    public static LocalDateTime getEndDateOfYearLDT(Integer minus) {
        return LocalDateTime.now()
                .with(lastDayOfYear())
                .toLocalDate()
                .atTime(LocalTime.MAX)
                .minusYears(minus);
    }

    public static LocalDateTime getStartDateOfMonthLDT(Integer minusMonth) {
        return LocalDateTime.now()
                .with(firstDayOfMonth())
                .toLocalDate()
                .atTime(LocalTime.MIN)
                .minusMonths(minusMonth);
    }

    public static LocalDateTime getEndDateOfMonthLDT(Integer minusMonth) {
        return LocalDateTime.now()
                .with(lastDayOfMonth())
                .toLocalDate()
                .atTime(LocalTime.MAX)
                .minusMonths(minusMonth);
    }

    public static LocalDateTime getStartDateOfDayLDT(Integer minusDay) {
        return LocalDateTime.now()
                .toLocalDate()
                .atTime(LocalTime.MIN)
                .minusDays(minusDay);
    }

    public static LocalDateTime getEndDateOfDayLDT(Integer minusDay) {
        return LocalDateTime.now()
                .toLocalDate()
                .atTime(LocalTime.MAX)
                .minusDays(minusDay);
    }

    public static LocalDate getEndDateOfYear() {
        return YearMonth.now().withMonth(12).atEndOfMonth();
    }

    public static LocalDateTime getStartTimeOfMonthBeforeXMonth() {
        return LocalDate.now().withDayOfMonth(1).atStartOfDay();
    }

    public static String getStartTimeOfWeekByTime(Long time) {
        /*if sunday -> old week*/
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        cal.setTimeInMillis(time);
        // Set the calendar to monday of the current week
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        return df.format(cal.getTime());
    }

    public static long getStartTimeOfWeek(Integer week, Integer year) {
        // Get calendar set to current date and time
        final long time = now()
                .withYear(year != null ? year : now().getYear())
                .withHour(0)
                .withMinute(0)
                .with(firstDayOfYear())
                .plusWeeks(week - 1)
                .getLong(INSTANT_SECONDS) * 1000;

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
        // Set the calendar to monday of the current week
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return c.getTime().getTime();
    }

    public static long getStartTimeOfWeek(Long time) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
        // Set the calendar to monday of the current week
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        String format = new SimpleDateFormat("dd-MM-yyyy").format(c.getTime().getTime());
        final String[] list = format.split("-");
        return now()
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withYear(Integer.parseInt(list[2]))
                .withMonth(Integer.parseInt(list[1]))
                .withDayOfMonth(Integer.parseInt(list[0]))
                .getLong(INSTANT_SECONDS) * 1000;
    }

    private static long getEndTimeOfWeek(Long time) {
        Calendar c = Calendar.getInstance();
        if (time != null) c.setTimeInMillis(time);
        // Set the calendar to monday of the current week
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        c.add(Calendar.DATE, 6);
        return c.getTime().getTime();
    }

    public static LocalDateTime getStartOfCurrentQuarter() {
        LocalDate now = LocalDate.now();
        Month startOfQuarterMonth;

        // Determine the start month of the current quarter
        if (now.getMonthValue() <= 3) {
            startOfQuarterMonth = Month.JANUARY;  // Q1: January 1
        } else if (now.getMonthValue() <= 6) {
            startOfQuarterMonth = Month.APRIL;    // Q2: April 1
        } else if (now.getMonthValue() <= 9) {
            startOfQuarterMonth = Month.JULY;     // Q3: July 1
        } else {
            startOfQuarterMonth = Month.OCTOBER;  // Q4: October 1
        }
        // Get the start of the quarter
        LocalDate startOfQuarter = now
                .withMonth(startOfQuarterMonth.getValue())
                .with(TemporalAdjusters.firstDayOfMonth());
        // Return start of the quarter as LocalDateTime at start of the day
        return startOfQuarter.atStartOfDay();
    }

    public static List<LocalDate> getDaysOfMonth() {
        YearMonth yearMonth = YearMonth.now();
        return yearMonth.atDay(1)
                .datesUntil(yearMonth.atEndOfMonth().plusDays(1))
                .toList();
    }

    /*
     *
     * Các func format, convert, parser date -> string, string -> date, ...
     *
     */
    public static String formatLocalDateTime(LocalDateTime value, String pattern) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            return value.format(formatter);
        } catch (Exception e) {
            log.error("formatLocalDateTime ERROR", e);
            return null;
        }
    }

    public static String formatLocalDate(LocalDate value, String pattern) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            return value.format(formatter);
        } catch (Exception e) {
            log.error("formatLocalDate ERROR", e);
            return null;
        }
    }

    public static String formatOffsetDateTime(OffsetDateTime value, String pattern) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            return value.format(formatter);
        } catch (Exception e) {
            log.error("formatOffsetDateTime ERROR", e);
            return null;
        }
    }

    public static String formatLongTime(Long time, String formatDate) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(formatDate);
            return format.format(new Date(time));
        } catch (Exception e) {
            log.error("formatLongTime ERROR", e);
            return null;
        }
    }

    public static LocalTime parseToLocalTime(String value, String pattern) {
        if (StringUtils.isEmpty(value)) return null;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            return LocalTime.parse(value, formatter);
        } catch (Exception e) {
            log.error("parseToLocalTime ERR", e);
            return null;
        }
    }

    public static LocalDateTime parseToLocalDateTime(String value, String pattern) {
        if (StringUtils.isEmpty(value)) return null;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            return LocalDateTime.parse(value, formatter);
        } catch (Exception e) {
            log.error("parseStringToLocalDateTime ERR", e);
            return null;
        }
    }

    public static LocalDateTime parseToLocalDateTime(String input) {
        if (input == null) return null;
        try {
            if (input.length() > 11) {
                return parseStringToLocalDateTime(input);
            }
            LocalDate localDate = parseToLocalDate(input);
            if (localDate == null) return null;
            return localDate.atStartOfDay()
                    .withHour(now().getHour())
                    .withMinute(now().getMinute());
        } catch (Exception e) {
            log.error("Fail to parser date time: {}", input, e);
            return null;
        }
    }

    public static LocalDate parseToLocalDate(String input) {
        try {
            DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            DateTimeFormatter formatter3 = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            DateTimeFormatter formatter4 = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            String[] inputSplit1 = input.split("-");
            if (inputSplit1.length == 3) {
                if (inputSplit1[0].length() == 4) {
                    return LocalDate.parse(input, formatter1);
                } else {
                    return LocalDate.parse(input, formatter2);
                }
            } else if (input.split("-").length == 2) {
                return LocalDate.parse(input.concat("-" + now().getYear()), formatter2);
            }

            String[] inputSplit2 = input.split("/");
            if (inputSplit2.length == 3) {
                if (inputSplit1[0].length() == 4) {
                    return LocalDate.parse(input, formatter3);
                } else {
                    return LocalDate.parse(input, formatter4);
                }
            } else if (inputSplit2.length == 2) {
                return LocalDate.parse(input.concat("-" + now().getYear()), formatter4);
            }
            return LocalDate.parse(input.concat("-" + now().getMonthValue() + "-" + now().getYear()), formatter2);
        } catch (Exception e) {
            return null;
        }
    }

    public static LocalDateTime parseStringToLocalDateTime(String input) {
        return Stream.of(
                        ofPattern(VIETNAM_DATE_TIME_PATTERN),
                        ofPattern(VIETNAM_DATE_TIME_2_PATTERN),
                        ofPattern(ISO_DATE_TIME_UTC_PATTERN),
                        ofPattern(ISO_DATE_TIME_UTC_2_PATTERN),
                        ofPattern(ISO_OFFSET_DATE_TIME_PATTERN),
                        ofPattern(DATE_TIME_PATTERN),
                        ofPattern(TIME_DATE_PATTERN))
                .map(dateTimeFormatter -> {
                    try {
                        return LocalDateTime.parse(input, dateTimeFormatter);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public static Long parseToEpochMilli(String input) {
        try {
            LocalDateTime localDateTime = parseToLocalDateTime(input);
            ZonedDateTime zdt = ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
            return zdt.toInstant().toEpochMilli();
        } catch (Exception e) {
            log.error(e);
            return null;
        }
    }

    public static Long localDateToEpochMilli(LocalDate localDate) {
        return localDate == null ? null : localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public static Long localDateTimeToEpochMilli(LocalDateTime localDateTime) {
        return localDateTime == null ? null : localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public static OffsetDateTime localDateTimeToOffset(LocalDateTime localDateTime) {
        return localDateTime == null ? null : localDateTime.atZone(ZoneId.systemDefault()).toOffsetDateTime();
    }

    public static LocalDate epochMilliToLocalDate(Long epochMilli) {
        if (epochMilli == null || epochMilli <= 0) return null;
        return Instant.ofEpochMilli(epochMilli).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static LocalDate epochMilliToLocalDateOrNow(Long epochMilli) {
        if (epochMilli == null || epochMilli <= 0) epochMilli = getCurrentEpochMilli();
        return Instant.ofEpochMilli(epochMilli).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static LocalDateTime epochMilliToLocalDateTime(Long epochMilli) {
        if (epochMilli == null || epochMilli < 0) return null;
        return Instant.ofEpochMilli(epochMilli).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static LocalDateTime epochMilliToLocalDateTimeOrNow(Long epochMilli) {
        if (epochMilli == null) epochMilli = getCurrentEpochMilli();
        return Instant.ofEpochMilli(epochMilli).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }


    /*
     *
     * Các func tính toán, check date
     *
     */
    public static Timestamp minusTime(Timestamp time, int amount, TemporalUnit unit) {
        return Timestamp.valueOf(time.toLocalDateTime().minus(amount, unit));
    }

    public static Long minuteToLong(Integer minute) {
        return (long) minute * 60 * 1000;
    }

    public static Timestamp incrementTimeWithDays(Timestamp time, Long numDays) {
        LocalDateTime localDateTime = time.toLocalDateTime().plusDays(numDays);
        LocalDateTime roundFloor = localDateTime.truncatedTo(ChronoUnit.DAYS);
        return Timestamp.valueOf(roundFloor);
    }

    public static boolean checkTimeBetween(Timestamp from, Timestamp to, Long timeCompare) {
        return timeCompare >= from.getTime() && timeCompare <= to.getTime();
    }

    public static Boolean isWeekend() {
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
    }

    public static boolean isStartDayOfMonth(Long time) {
        return !getMonth(time).equals(getMonth(time - getOneDayMini()));
    }

    public static boolean isEndDayOfMonth(Long time) {
        return !getMonth(time).equals(getMonth(time + getOneDayMini()))
                || time >= getStartTimeOfDay();
    }

    public static Long getMinutesBetween(LocalDateTime start, LocalDateTime end) {
        return Duration.between(start, end).toMinutes();
    }

    public static LocalDateTime getTimeBeforeXDayLDT(Integer numDayBefore) {
        return now().minusDays(numDayBefore).toLocalDateTime();
    }

    public static long getStartTimeBeforeXDay(Integer numDayBefore) {
        return LocalDate.now().minusDays(numDayBefore).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public static LocalDateTime getStartTimeBeforeXDayLDT(Integer numDayBefore) {
        return LocalDate.now().minusDays(numDayBefore).atStartOfDay(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static long getEndTimeBeforeXDay(Integer numDayBefore) {
        return LocalDate.now().minusDays(numDayBefore).atTime(LocalTime.MAX)
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public static LocalDateTime getEndTimeBeforeXDayLDT(Integer numDayBefore) {
        return LocalDate.now().minusDays(numDayBefore).atTime(LocalTime.MAX)
                .atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static Long getStartTimeBeforeXDayFromTime(Long time, Integer numDayBefore) {
        ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
        return zdt.withHour(0).withMinute(0).withSecond(0)
                .minusDays(numDayBefore)
                .toInstant().toEpochMilli();
    }

    public static long getStartTimeOfMonthBeforeXMonth(Integer preMonth) {
        return LocalDate.now().minusMonths(preMonth).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public static LocalDateTime getStartTimeOfMonthBeforeXMonthLDT(Integer numMonth) {
        return LocalDate.now().minusMonths(numMonth).withDayOfMonth(1).atStartOfDay();
    }

    public static LocalDateTime getStartTimeOfYearBeforeXYearLDT(Integer numYear) {
        return LocalDate.now().minusYears(numYear).withDayOfMonth(1).atStartOfDay();
    }

    public static String getLabel(FrequencyEnum frequencyEnum, LocalDateTime localDateTime) {
        int month = localDateTime.getMonthValue();
        int quarter = (month - 1) / 3 + 1;
        int year = localDateTime.getYear();
        return switch (frequencyEnum) {
            case QUARTER -> "Q" + quarter + "-" + year;
            case DAY, HOUR, MONTH, DAY_OF_MONTH ->
                    localDateTime.format(DateTimeFormatter.ofPattern(frequencyEnum.pattern()));
            default -> "";
        };
    }

    public static Map<String, Long> getMapTimeLabelsByFrequency(FrequencyEnum frequency, LocalDateTime from, LocalDateTime to) {
        Map<String, Long> labelTimeMap = new LinkedHashMap<>();
        LocalDateTime current = from;
        switch (frequency) {
            case MONTH -> {
                while (current.isBefore(to) || current.isEqual(to)) {
                    String label = current.format(DateTimeFormatter.ofPattern(frequency.pattern()));
                    labelTimeMap.put(label, localDateTimeToEpochMilli(current));
                    current = current.plusMonths(1);
                }
            }
            case QUARTER -> {
                while (current.isBefore(to) || current.isEqual(to)) {
                    int quarter = (current.getMonthValue() - 1) / 3 + 1;
                    String label = "Q" + quarter + "-" + current.format(DateTimeFormatter.ofPattern("yyyy"));
                    labelTimeMap.put(label, localDateTimeToEpochMilli(current));
                    current = current.plusMonths(3);
                }
            }
            case DAY, DAY_OF_MONTH -> {
                while (current.isBefore(to) || current.isEqual(to)) {
                    String label = current.format(DateTimeFormatter.ofPattern(frequency.pattern()));
                    labelTimeMap.put(label, localDateTimeToEpochMilli(current));
                    current = current.plusDays(1);
                }
            }
            case HOUR -> {
                while (current.isBefore(to) || current.isEqual(to)) {
                    String label = current.format(DateTimeFormatter.ofPattern(frequency.pattern()));
                    labelTimeMap.put(label, localDateTimeToEpochMilli(current));
                    current = current.plusHours(1);
                }
            }
            default -> {
                return labelTimeMap;
            }
        }
        return labelTimeMap;
    }

    public static List<String> getListTimeLabelsByFrequency(FrequencyEnum frequency, LocalDateTime from, LocalDateTime to) {
        return getMapTimeLabelsByFrequency(frequency, from, to).keySet()
                .stream().toList();
    }


    public static List<String> getStartMonthsBetween(LocalDateTime from, LocalDateTime to) {
        List<String> startMonths = new ArrayList<>();
        LocalDateTime current = from;
        while (current.isBefore(to) || current.isEqual(to)) {
            startMonths.add(current.format(DateTimeFormatter.ofPattern("MM-yyyy")));
            current = current.plusMonths(1);
        }
        return startMonths;
    }

    public static List<String> getStartMonthsBetween(LocalDate from, LocalDate to) {
        List<String> startMonths = new ArrayList<>();
        LocalDate current = from;
        while (current.isBefore(to) || current.isEqual(to)) {
            startMonths.add(current.format(DateTimeFormatter.ofPattern("MM-yyyy")));
            current = current.plusMonths(1);
        }
        return startMonths;
    }

    public static List<String> getStartWeeksBetween(LocalDateTime from, LocalDateTime to) {
        List<String> startWeeks = new ArrayList<>();
        LocalDateTime current = from;

        if (from.getDayOfWeek() != DayOfWeek.SUNDAY) {
            startWeeks.add("Week " + getWeekOfYear(current));
        }

        while (current.isBefore(to) || current.isEqual(to)) {
            DayOfWeek dayOfWeek = current.getDayOfWeek();

            if (dayOfWeek == DayOfWeek.SUNDAY) {
                startWeeks.add("Week " + getWeekOfYear(current));
            }
            current = current.plusDays(1);
        }
        return startWeeks;
    }

    public static List<Pair<String, Long>> getStartWeeksBetweenPair(LocalDateTime from, LocalDateTime to) {
        List<Pair<String, Long>> startWeeks = new ArrayList<>();
        LocalDateTime current = from;

        while (from.getDayOfWeek() != DayOfWeek.SUNDAY) {
            current = current.minusDays(1);
            if (current.getDayOfWeek() == DayOfWeek.SUNDAY)
                break;
        }
        while (current.isBefore(to) || current.isEqual(to)) {
            DayOfWeek dayOfWeek = current.getDayOfWeek();

            if (dayOfWeek == DayOfWeek.SUNDAY) {
                startWeeks.add(Pair.of("Week " + getWeekOfYear(current),
                        localDateTimeToEpochMilli(current)));
            }
            current = current.plusDays(1);
        }
        return startWeeks;
    }

    public static String getWeek(Date day) {
        return "Week " + getWeekOfYear(day);
    }

    private static int getWeekOfYear(LocalDateTime current) {
        Instant instant = current.atZone(ZoneId.systemDefault()).toInstant();
        Date date = Date.from(instant);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.WEEK_OF_YEAR);
    }

    private static int getWeekOfYear(Date current) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(current);
        return calendar.get(Calendar.WEEK_OF_YEAR);
    }

    public static List<String> getStartDaysBetween(LocalDateTime from, LocalDateTime to) {
        List<String> startDays = new ArrayList<>();
        LocalDateTime current = from;
        while (current.isBefore(to) || current.isEqual(to)) {
            startDays.add(current.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
            current = current.plusDays(1);
        }
        return startDays;
    }

    public static List<Pair<String, Long>> getStartHourBetween(LocalDateTime from, LocalDateTime to) {
        List<Pair<String, Long>> startHours = new ArrayList<>();
        LocalDateTime current = from;
        if (current.getMinute() != 0)
            current = current.withMinute(0).plusHours(1);


        while (current.isBefore(to) || current.isEqual(to)) {
            startHours.add(Pair.of(formatLocalDateTime(current, "HH-dd-MM-yyyy"),
                    localDateTimeToEpochMilli(current)));
            current = current.plusHours(1);
        }
        return startHours;
    }

    public static long getEndTimeOfWeek(Integer week, Integer year) {
        // Get calendar set to current date and time
        final long time = now()
                .withYear(year != null ? year : now().getYear())
                .withHour(0)
                .withMinute(0)
                .with(firstDayOfYear())
                .plusWeeks(week - 1)
                .getLong(INSTANT_SECONDS) * 1000;

        return getEndTimeOfWeek(time);
    }

    public static long getFirstDayOfWeek(Integer week, Integer year) {
        return now()
                .withYear(year != null ? year : now().getYear())
                .with(firstDayOfYear())
                .plusWeeks(week)
                .minusDays(2) // Hiện tại tuần bị lệch 2 ngày, report tuần từ t2 -> chủ nhật nên phải trừ đi 1 ngày
                .getLong(INSTANT_SECONDS) * 1000;
    }

    public static Integer countNumDays(Long startTime, Long endTime) {
        final Set<Long> dayOfMonth = new HashSet<>();
        for (Long start = startTime; start < endTime; start += getOneDayMini()) {
            dayOfMonth.add(getStartTimeOfDayFromTime(start));
        }
        return dayOfMonth.size();
    }

    public static LocalTime getLocalTimeFromSecond(Integer second) {
        if (second == null || second < 0) return null;
        try {
            return LocalTime.ofSecondOfDay(second);
        } catch (Exception e) {
            return null;
        }
    }

}