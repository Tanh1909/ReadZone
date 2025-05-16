package vn.tnteco.mqtt.consumer;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilter;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.ThreadContext;
import vn.tnteco.mqtt.config.MqttClientConfig;
import vn.tnteco.mqtt.config.properties.MqttProperties;
import vn.tnteco.mqtt.data.MqttResponse;
import vn.tnteco.mqtt.data.TrackingContextEnum;
import vn.tnteco.mqtt.interceptor.IMqttMessageInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Log4j2
public abstract class BaseConsumer {

    protected Mqtt5AsyncClient mqtt5Client;

    @PostConstruct
    protected void subscribe() {
        String clientId = MqttClientConfig.getMqttProperties().getServer().getUsername()
                + "___" + getMqttTopicFilter().toString() + "___" + UUID.randomUUID();
        mqtt5Client = MqttClientConfig.buildMqtt5AsyncClient(clientId);
        mqtt5Client.connect()
                .thenCompose(connAck -> mqtt5Client.subscribeWith()
                        .topicFilter(this.getMqttTopicFilter())
                        .qos(this.mqttQos())
                        .callback(this::executePublish)
                        .manualAcknowledgement(true)
                        .send())
                .thenRun(() -> {
                    log.info("Subscribed successfully to {}", this.getMqttTopicFilter());
                })
                .exceptionally(throwable -> {
                    log.error("Subscribed error to {}", this.getMqttTopicFilter(), throwable);
                    return null;
                });
    }

    @PreDestroy
    protected void unsubscribe() {
        mqtt5Client.unsubscribeWith().topicFilter(this.getMqttTopicFilter()).send()
                .thenCompose(unsubAck -> mqtt5Client.disconnect())
                .thenRun(() -> {
                    log.info("Unsubscribed successfully to {}", this.getMqttTopicFilter());
                })
                .exceptionally(throwable -> {
                    log.error("Unsubscribed error to {}", this.getMqttTopicFilter(), throwable);
                    return null;
                });
    }

    public abstract MqttQos mqttQos();

    public abstract MqttTopicFilter getMqttTopicFilter();

    public abstract void process(MqttResponse data);

    public abstract void processIfFailed(String message);

    private IMqttMessageInterceptor interceptor() {
        MqttProperties.Interceptor interceptor = MqttClientConfig.getMqttProperties().getInterceptor();
        if (interceptor == null || !Boolean.TRUE.equals(interceptor.getEnable())) {
            return mqttResponse -> {};
        }
        return this.getInterceptor();
    }

    protected IMqttMessageInterceptor getInterceptor() {
        return Optional.ofNullable(MqttClientConfig.getMqttMessageInterceptor()).orElse(mqttResponse -> {});
    }

    protected void executePublish(Mqtt5Publish publish) {
        MqttTopicFilter mqttTopicFilter = this.getMqttTopicFilter();
        String correlationId = mqttTopicFilter.toString() + "-" + UUID.randomUUID().toString().replace("-", "").toLowerCase();
        ThreadContext.put(TrackingContextEnum.CORRELATION_ID.getKey(), correlationId);
        log.debug("[MQTT5] Start processing msg topic [{}]", mqttTopicFilter);
        MqttResponse.Meta mqttMetaData = this.getMqttMetaData(publish);
        log.debug("[MQTT5] Meta: {}", mqttMetaData.toString());
        String message = new String(publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
        log.debug("[MQTT5] Payload: {}", message);
        try {
            MqttResponse mqttResponse = new MqttResponse().setMessage(message).setMeta(mqttMetaData);
            this.interceptor().invoke(mqttResponse);
            this.process(mqttResponse);
            publish.acknowledge();
        } catch (Exception e) {
            log.error("[MQTT5] executePublish ERROR: {}", e.getMessage(), e);
            this.processIfFailed(message);
            publish.acknowledge();
        } finally {
            log.debug("[MQTT5] End processing msg topic [{}]", mqttTopicFilter);
            ThreadContext.clearAll();
        }
    }

    private MqttResponse.Meta getMqttMetaData(Mqtt5Publish publish) {
        String correlationId = publish.getCorrelationData()
                .map(correlationData -> {
                    byte[] x = new byte[correlationData.capacity()];
                    correlationData.get(x);
                    return new String(x);
                })
                .orElse(null);
        Map<String, String> headers = publish.getUserProperties().asList()
                .stream()
                .collect(Collectors.toMap(e -> e.getName().toString(), e -> e.getValue().toString()));
        return new MqttResponse.Meta()
                .setTopic(publish.getTopic().toString())
                .setResponseTopic(publish.getResponseTopic().toString())
                .setMqttQos(publish.getQos())
                .setRetain(publish.isRetain())
                .setMessageType(publish.getType())
                .setContentType(publish.getContentType().map(Object::toString).orElse(null))
                .setCorrelationId(correlationId)
                .setHeaders(headers);
    }

}
