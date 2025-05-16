package vn.tnteco.common.core.context;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import vn.tnteco.common.core.model.SimpleSecurityUser;
import vn.tnteco.common.core.model.UserPrincipal;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SecurityContext {

    private static final ThreadLocal<Map<String, Object>> contextManage = new ThreadLocal<>();

    private static final String AUTHENTICATION = "AUTHENTICATION";
    private static final String USER_PRINCIPAL = "USER_PRINCIPAL";
    private static final String ACCESS_TOKEN = "ACCESS_TOKEN";

    public static void setSimpleSecurityUser(SimpleSecurityUser simpleSecurityUser) {
        set(AUTHENTICATION, simpleSecurityUser);
    }

    public static void setUserPrincipal(UserPrincipal userPrincipal) {
        set(USER_PRINCIPAL, userPrincipal);
    }

    public static SimpleSecurityUser getSimpleSecurityUser() {
        if (contextManage.get() != null &&
                contextManage.get().containsKey(AUTHENTICATION)) {
            return (SimpleSecurityUser) contextManage.get().get(AUTHENTICATION);
        }
        return null;
    }

    public static UserPrincipal getUserPrincipal() {
        if (contextManage.get() != null &&
                contextManage.get().containsKey(USER_PRINCIPAL)) {
            return (UserPrincipal) contextManage.get().get(USER_PRINCIPAL);
        }
        return null;
    }

    public static void setContextManage(Map<String, Object> context) {
        contextManage.set(context);
    }

    public static Map<String, Object> getContextManage() {
        return contextManage.get();
    }

    public static void setAccessToken(String accessToken) {
        set(ACCESS_TOKEN, accessToken);
    }

    public static String getAccessToken() {
        if (contextManage.get() != null &&
                contextManage.get().containsKey(ACCESS_TOKEN)) {
            return (String) contextManage.get().get(AUTHENTICATION);
        }
        return null;
    }

    private static void set(String key, Object value) {
        if (contextManage.get() != null) {
            Map<String, Object> dataMap = contextManage.get();
            dataMap.put(key, value);
        } else {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put(key, value);
            contextManage.set(dataMap);
        }
    }

    public static void clearContext() {
        contextManage.remove();
    }

}
