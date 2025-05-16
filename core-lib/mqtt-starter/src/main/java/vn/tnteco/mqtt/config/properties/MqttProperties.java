package vn.tnteco.mqtt.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "messaging.mqtt")
public class MqttProperties {

    private Boolean enable;

    private Server server;

    private Interceptor interceptor;

    @Getter
    @Setter
    public static class Server {
        private String protocol;
        private String host;
        private Integer port;
        private Integer sslPort;
        private Integer wsPort;
        private Integer wssPort;
        private String username;
        private String password;
        private String serverPath = "/mqtt";
        private String queryString = "";
    }

    @Getter
    @Setter
    public static class Interceptor {
        private Boolean enable;
    }

}
