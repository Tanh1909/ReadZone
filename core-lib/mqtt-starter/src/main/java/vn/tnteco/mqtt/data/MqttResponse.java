package vn.tnteco.mqtt.data;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.message.Mqtt5MessageType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Map;

@Getter
@Setter
@Accessors(chain = true)
public class MqttResponse {

    private String message;

    private Meta meta;

    @Getter
    @Setter
    @ToString
    @Accessors(chain = true)
    public static class Meta {

        private String topic;

        private String responseTopic;

        private MqttQos mqttQos;

        private boolean isRetain;

        private Mqtt5MessageType messageType;

        private String contentType;

        private String correlationId;

        private Map<String, String> headers;

    }
}
