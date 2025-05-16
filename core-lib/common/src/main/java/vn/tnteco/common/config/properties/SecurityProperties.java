package vn.tnteco.common.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Setter
@Getter
@Configuration
@ConfigurationProperties("app.security")
public class SecurityProperties {

    private Set<String> apiWhitelist = new HashSet<>(List.of(
            "/swagger-ui", "/springdoc", "/v3/api-docs", "/actuator/health"
    ));

    private String apiCheckPermissionUrl;

    private String serverKey;

    private String apiKey;

    private Jwt jwt;

    @Setter
    @Getter
    public static class Jwt {
        private String secretKey;
        private Integer expiredIn; //ms
    }

    @Value("${app.security.apiWhitelist:}")
    public void setApiWhitelist(String[] apiWhitelistArray) {
        if (apiWhitelistArray != null && apiWhitelistArray.length > 0) {
            this.apiWhitelist.addAll(Arrays.asList(apiWhitelistArray));
        }
    }

}
