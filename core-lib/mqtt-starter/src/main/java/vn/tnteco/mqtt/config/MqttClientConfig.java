package vn.tnteco.mqtt.config;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttClientBuilder;
import com.hivemq.client.mqtt.lifecycle.MqttClientConnectedListener;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedListener;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import vn.tnteco.mqtt.config.properties.MqttProperties;
import vn.tnteco.mqtt.interceptor.IMqttMessageInterceptor;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Log4j2
@Configuration
@ConditionalOnProperty(
        value = {"messaging.mqtt.enable"},
        havingValue = "true"
)
public class MqttClientConfig {

    @Getter
    @Setter
    private static Mqtt5AsyncClient mqtt5AsyncClient;

    @Getter
    @Setter
    private static MqttProperties mqttProperties;

    @Getter
    @Setter
    private static IMqttMessageInterceptor mqttMessageInterceptor;

    public MqttClientConfig(@Autowired MqttProperties mqttProperties,
                            @Autowired(required = false) IMqttMessageInterceptor interceptor) {
        setMqttProperties(mqttProperties);
        setMqttMessageInterceptor(interceptor);
    }

    @Primary
    @Bean("mqtt5AsyncClient")
    public Mqtt5AsyncClient mqtt5AsyncClient() {
        String clientId = mqttProperties.getServer().getUsername() + "___all___" + UUID.randomUUID();
        Mqtt5AsyncClient mqttClient = buildMqtt5AsyncClient(clientId);
        mqttClient.connect();
        setMqtt5AsyncClient(mqttClient);
        return mqttClient;
    }

    public static Mqtt5AsyncClient buildMqtt5AsyncClient(String clientId) {
        MqttProperties.Server mqttServerProperties = mqttProperties.getServer();
        MqttClientBuilder mqttClientBuilder = buildMqttClient(mqttServerProperties, clientId);
        return mqttClientBuilder.useMqttVersion5()
                .simpleAuth()
                .username(mqttServerProperties.getUsername())
                .password(mqttServerProperties.getPassword().getBytes())
                .applySimpleAuth()
                .buildAsync();
    }

    private static MqttClientBuilder buildMqttClient(MqttProperties.Server mqttServer, String clientId) {
        MqttClientBuilder mqttClientBuilder = MqttClient.builder()
                .identifier(clientId)
                .automaticReconnect()
                .initialDelay(30, TimeUnit.SECONDS)
                .maxDelay(5, TimeUnit.MINUTES)
                .applyAutomaticReconnect()
                .addConnectedListener(mqttClientConnectedListener)
                .addDisconnectedListener(mqttClientDisconnectedListener);
        return switch (mqttServer.getProtocol()) {
            case "mqtt" -> mqttClientBuilder
                    .transportConfig()
                    .serverHost(mqttServer.getHost())
                    .serverPort(mqttServer.getPort())
                    .mqttConnectTimeout(30, TimeUnit.SECONDS)
                    .applyTransportConfig();
            case "mqtts" -> mqttClientBuilder
                    .transportConfig()
                    .serverHost(mqttServer.getHost())
                    .serverPort(mqttServer.getSslPort())
                    .mqttConnectTimeout(30, TimeUnit.SECONDS)
                    .applyTransportConfig()
                    .sslConfig()
                    .applySslConfig();
            case "ws" -> mqttClientBuilder
                    .transportConfig()
                    .serverHost(mqttServer.getHost())
                    .serverPort(mqttServer.getWsPort())
                    .webSocketConfig()
                    .serverPath(mqttServer.getServerPath())
                    .queryString(mqttServer.getQueryString())
                    .handshakeTimeout(10, TimeUnit.SECONDS)
                    .applyWebSocketConfig()
                    .applyTransportConfig();
            case "wss" -> mqttClientBuilder
                    .transportConfig()
                    .serverHost(mqttServer.getHost())
                    .serverPort(mqttServer.getWssPort())
                    .webSocketConfig()
                    .serverPath(mqttServer.getServerPath())
                    .queryString(mqttServer.getQueryString())
                    .handshakeTimeout(10, TimeUnit.SECONDS)
                    .applyWebSocketConfig()
                    .sslWithDefaultConfig()
                    .applyTransportConfig();
            default -> throw new UnsupportedOperationException("Mqtt unsupported protocol");
        };
    }

    private static final MqttClientConnectedListener mqttClientConnectedListener = connectedContext -> {
        log.info("Client {} is connected!", connectedContext.getClientConfig().getClientIdentifier());
    };

    private static final MqttClientDisconnectedListener mqttClientDisconnectedListener = disconnectedContext -> {
        log.info("Client {} is disconnected!", disconnectedContext.getClientConfig().getClientIdentifier());
        if (disconnectedContext.getReconnector().getAttempts() > 0) {
            log.error("Client reconnect mqtt error: {} !!!", disconnectedContext.getCause().getMessage());
        }
    };
}
