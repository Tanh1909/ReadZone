package vn.tnteco.spring.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;
import vn.tnteco.common.core.context.SecurityContext;
import vn.tnteco.common.core.model.SimpleSecurityUser;
import vn.tnteco.common.data.constant.HeaderConstant;

import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpUtils {

    public static HttpHeaders generateApiKeyHeader(String apiKey) {
        SimpleSecurityUser simpleSecurityUser = Objects.requireNonNull(SecurityContext.getSimpleSecurityUser());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HeaderConstant.API_KEY, apiKey);
        httpHeaders.add(HeaderConstant.ORG_ID, simpleSecurityUser.getOrgId().toString());
        httpHeaders.add(HeaderConstant.USER_ID, simpleSecurityUser.getId().toString());
        return httpHeaders;
    }
}
