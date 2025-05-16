package vn.tnteco.spring.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class RequestUtils {

    private RequestUtils() {
    }

    private static final Logger logger = LogManager.getLogger(RequestUtils.class);

    public static String getRemoteIP(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("X-FORWARDED-FOR"))
                .orElse(request.getRemoteAddr());
    }

//    public static boolean matches(String ip, String subnet) {
//        try {
//            IpAddressMatcher ipAddressMatcher = new IpAddressMatcher(subnet);
//            return ipAddressMatcher.matches(ip);
//        } catch (Exception e) {
//            logger.error("[PARSER-IP] ip: {}, subnet: {}", ip, subnet, e);
//            return false;
//        }
//    }
}
