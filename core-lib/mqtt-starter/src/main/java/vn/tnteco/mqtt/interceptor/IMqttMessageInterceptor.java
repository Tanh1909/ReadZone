package vn.tnteco.mqtt.interceptor;

import vn.tnteco.mqtt.data.MqttResponse;

@FunctionalInterface
public interface IMqttMessageInterceptor {

    void invoke(MqttResponse mqttResponse);

}
