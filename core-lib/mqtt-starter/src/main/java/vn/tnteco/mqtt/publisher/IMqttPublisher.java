package vn.tnteco.mqtt.publisher;

import com.hivemq.client.mqtt.datatypes.MqttQos;

public interface IMqttPublisher {

    <T> void pushAsync(String topic, T message);

    <T> void pushAsync(String topic, MqttQos qos, T message);

}
