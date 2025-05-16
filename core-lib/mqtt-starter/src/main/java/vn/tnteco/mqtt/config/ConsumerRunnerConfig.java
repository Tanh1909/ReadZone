package vn.tnteco.mqtt.config;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import lombok.Data;
import lombok.experimental.Accessors;
import vn.tnteco.mqtt.data.MqttResponse;
import vn.tnteco.mqtt.interceptor.IMqttMessageInterceptor;

import java.util.function.Consumer;

@Data
@Accessors(chain = true)
public class ConsumerRunnerConfig {

    private String id;

    private String shareName;

    private String topicName;

    private MqttQos mqttQos;

    private IMqttMessageInterceptor interceptor;

    private Consumer<MqttResponse> callback = data -> {};

    private Consumer<String> processMessageFailed = message -> {};

}
