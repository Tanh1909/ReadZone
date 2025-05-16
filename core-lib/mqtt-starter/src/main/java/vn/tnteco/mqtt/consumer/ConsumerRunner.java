package vn.tnteco.mqtt.consumer;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.datatypes.MqttSharedTopicFilter;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilter;
import lombok.extern.log4j.Log4j2;
import vn.tnteco.mqtt.config.ConsumerRunnerConfig;
import vn.tnteco.mqtt.config.MqttClientConfig;
import vn.tnteco.mqtt.data.MqttResponse;
import vn.tnteco.mqtt.interceptor.IMqttMessageInterceptor;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Log4j2
public class ConsumerRunner extends BaseConsumer implements Runnable {

    private final String consumerId;

    private final ConsumerRunnerConfig config;

    private final AtomicBoolean closed = new AtomicBoolean(false);

    public ConsumerRunner(String consumerId, ConsumerRunnerConfig config) {
        this.consumerId = consumerId;
        this.config = config;
    }

    @Override
    protected void subscribe() {
        String clientId = MqttClientConfig.getMqttProperties().getServer().getUsername() + "___" + consumerId + "___" + UUID.randomUUID();
        mqtt5Client = MqttClientConfig.buildMqtt5AsyncClient(clientId);
        mqtt5Client.connect()
                .thenCompose(connAck -> mqtt5Client.subscribeWith()
                        .topicFilter(this.getMqttTopicFilter())
                        .qos(this.mqttQos())
                        .callback(this::executePublish)
                        .manualAcknowledgement(true)
                        .send())
                .thenRun(() -> {
                    closed.set(false);
                    log.info("Consumer Runner Subscribed successfully to {}", this.getMqttTopicFilter());
                })
                .exceptionally(throwable -> {
                    closed.set(true);
                    log.error("Consumer Runner Subscribed error to {}", this.getMqttTopicFilter(), throwable);
                    return null;
                });
    }

    @Override
    protected void unsubscribe() {
        mqtt5Client.unsubscribeWith().topicFilter(this.getMqttTopicFilter()).send()
                .thenCompose(unsubAck -> mqtt5Client.disconnect())
                .thenRun(() -> {
                    closed.set(true);
                    log.info("Unsubscribed successfully to {}", this.getMqttTopicFilter());
                })
                .exceptionally(throwable -> {
                    closed.set(false);
                    log.error("Unsubscribed error to {}", this.getMqttTopicFilter(), throwable);
                    return null;
                });
    }

    @Override
    public void run() {
        log.info("[MQTT5] RUN Consumer Runner: {}, config: {}", consumerId, config.toString());
        try {
            this.subscribe();
        } catch (Exception e) {
            log.error("[MQTT5] RUN Consumer Runner: {} ERROR: {}", consumerId, e.getMessage(), e);
        }
    }

    @Override
    public MqttQos mqttQos() {
        return config.getMqttQos();
    }

    @Override
    public MqttTopicFilter getMqttTopicFilter() {
        return MqttSharedTopicFilter.of(config.getShareName(), config.getTopicName());
    }

    @Override
    public void process(MqttResponse data) {
        config.getCallback().accept(data);
    }

    @Override
    public void processIfFailed(String message) {
        config.getProcessMessageFailed().accept(message);
    }

    @Override
    protected IMqttMessageInterceptor getInterceptor() {
        return Optional.ofNullable(config.getInterceptor()).orElse(super.getInterceptor());
    }

    public void stop() {
        log.info("[MQTT5] STOP Consumer Runner: {}", consumerId);
        this.unsubscribe();
    }

    public boolean isStop() {
        return closed.get();
    }
}
