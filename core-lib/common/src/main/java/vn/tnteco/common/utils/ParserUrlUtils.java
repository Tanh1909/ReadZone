package vn.tnteco.common.utils;

import lombok.extern.log4j.Log4j2;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URLEncodedUtils;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
public class ParserUrlUtils {
    public static String getAbsoluteUrl(String urlStr, boolean containParams) {
        if (urlStr == null || urlStr.equals("")) return null;
        try {
            URL url = new URL(urlStr);
            String absolutePath = url.getPath();
            String query = url.getQuery();
            if (containParams && query != null) absolutePath = absolutePath + "?" + query;
            if (absolutePath.equals("")) return "/";
            return absolutePath;
        } catch (Exception e) {
            log.error("Malformed url exception {}", urlStr, e);
        }
        return null;
    }

    public static String getHostname(String urlStr) {
        if (urlStr == null || urlStr.equals("")) return null;
        try {
            URL url = new URL(urlStr);
            return url.getHost();
        } catch (Exception e) {
            log.error("Malformed url exception {}", urlStr, e);
        }
        return null;
    }

    public static Map<String, String> extractUrlParams(String urlStr) {
        Map<String, String> paramsMap = new HashMap<>();
        if (urlStr == null || urlStr.equals("")) return paramsMap;
        try {
            List<NameValuePair> params = URLEncodedUtils.parse(urlStr, StandardCharsets.UTF_8);
            params.forEach(nameValuePair -> {
                String name = nameValuePair.getName();
                String value = nameValuePair.getValue();
                if (name != null && name.contains("?")) name = name.split("\\?")[1];
                if (value != null && value.contains("#")) value = value.split("#")[0];
                if (name != null && value != null) paramsMap.put(name, value);
            });
            return paramsMap;
        } catch (Exception e) {
            log.warn("Malformed url exception {}", urlStr, e);
        }
        return paramsMap;
    }

    public static String getParam(String urlStr, String paramName) {
        return extractUrlParams(urlStr).getOrDefault(paramName, null);
    }

}
