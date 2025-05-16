package vn.tnteco.mqtt.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.PropertyNamingStrategies.LOWER_CAMEL_CASE;

@Log4j2
@Component
@ConditionalOnProperty(
        value = {"messaging.mqtt.enable"},
        havingValue = "true"
)
public class Mqtt5PublisherImpl implements IMqttPublisher {

    @Qualifier("mqtt5AsyncClient")
    private final Mqtt5AsyncClient mqtt5AsyncClient;

    private final ObjectMapper objectMapper;

    public Mqtt5PublisherImpl(Mqtt5AsyncClient mqtt5AsyncClient) {
        this.mqtt5AsyncClient = mqtt5AsyncClient;
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .setPropertyNamingStrategy(LOWER_CAMEL_CASE)
                .configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    @SneakyThrows
    public <T> void pushAsync(String topic, T message) {
        this.pushAsync(topic, MqttQos.AT_MOST_ONCE, message);
    }

    @Override
    @SneakyThrows
    public <T> void pushAsync(String topic, MqttQos qos, T message) {
        log.debug("push event with topic: [{}], qos: [{}], message: {}", topic, qos, objectMapper.writeValueAsString(message));
        Mqtt5Publish publish = Mqtt5Publish.builder()
                .topic(topic)
                .payload(objectMapper.writeValueAsBytes(message))
                .qos(qos)
                .build();
        this.mqtt5AsyncClient.publish(publish);
    }

}
