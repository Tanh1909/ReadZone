package com.example.app.data.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CacheConstant {

    private static final String LOCK_CACHE_KEY_BASE = "LOCK";
    private static final String CACHE_KEY_BASE = "APP";
    private static final String CACHE_KEY_SEPARATOR = "::";

    private static String getCacheKey(String key, String... params) {
        StringBuilder sb = new StringBuilder(CACHE_KEY_BASE)
                .append(CACHE_KEY_SEPARATOR)
                .append(key);
        for (String param : params) {
            sb.append(CACHE_KEY_SEPARATOR).append(param);
        }
        return sb.toString();
    }

    private static String getLockCacheKey(String key, String... params) {
        StringBuilder sb = new StringBuilder(LOCK_CACHE_KEY_BASE)
                .append(CACHE_KEY_SEPARATOR)
                .append(CACHE_KEY_BASE)
                .append(CACHE_KEY_SEPARATOR)
                .append(key);
        for (String param : params) {
            sb.append(CACHE_KEY_SEPARATOR).append(param);
        }
        return sb.toString();
    }

    public static String getRegisterConfirmCode(String email) {
        return getCacheKey("REGISTER_CODE", email);
    }

    public static String getPaymentKey(String orderId) {
        return getCacheKey("PAYMENT", orderId);
    }

    public static String getViewBookKey(Integer bookId) {
        return getCacheKey("VIEW_BOOK", String.valueOf(bookId));
    }

    public static String getCacheStockKey(Integer bookId) {
        return getCacheKey("BOOK_STOCK_AVAILABLE", bookId.toString());
    }

    public static String getLockPaymentKey(Integer orderId) {
        return getLockCacheKey("PAYMENT", orderId.toString());
    }

}
