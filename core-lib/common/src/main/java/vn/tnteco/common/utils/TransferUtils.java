package vn.tnteco.common.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import vn.tnteco.common.core.json.JsonObject;
import vn.tnteco.common.data.constant.CommonConstant;

import java.nio.charset.StandardCharsets;
import java.util.function.Function;

@Log4j2
@UtilityClass
public class TransferUtils {

    public static Integer safeParseInteger(String value) {
        return safeParseInteger(value, null);
    }

    public static Integer safeParseInteger(String value, Integer defaultValue) {
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            log.error("safeParseInteger err", e);
            return defaultValue;
        }
    }

    public static Long safeParseLong(String value) {
        return safeParseLong(value, null);
    }

    public static Long safeParseLong(String value, Long defaultValue) {
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            log.error("safeParseLong err", e);
            return defaultValue;
        }
    }

    public static Double safeParseDouble(String value) {
        return safeParseDouble(value, null);
    }

    public static Double safeParseDouble(String value, Double defaultValue) {
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            log.error("parseDouble err", e);
            return defaultValue;
        }
    }

    public static Float safeParseFloat(String value) {
        return safeParseFloat(value, null);
    }

    public static Float safeParseFloat(String value, Float defaultValue) {
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(value);
        } catch (Exception e) {
            log.error("safeParseFloat err", e);
            return defaultValue;
        }
    }

    public static Short safeParseShort(String value) {
        return safeParseShort(value, 0);
    }

    public static Short safeParseShort(String value, int defaultValue) {
        if (StringUtils.isEmpty(value)) {
            return (short) defaultValue;
        }
        try {
            return Short.parseShort(value);
        } catch (Exception e) {
            log.error("safeParseShort err", e);
            return (short) defaultValue;
        }
    }

    public static String safeToString(Object obj) {
        if (ObjectUtils.isEmpty(obj)) {
            return CommonConstant.EMPTY_STRING;
        }
        return String.valueOf(obj);
    }

    public static String convertByteToString(byte[] data) {
        return new String(data, StandardCharsets.UTF_8);
    }

    public static <T> Function<JsonObject, T> mapToObject(Class<T> tClass) {
        return obj -> obj.mapTo(tClass);
    }

    public static <T> Function<JsonObject, T> mapToObject(TypeReference<T> typeReference) {
        return obj -> obj.mapTo(typeReference);
    }

}
