package vn.tnteco.mqtt.consumer;

import jakarta.annotation.PostConstruct;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import vn.tnteco.mqtt.config.ConsumerRunnerConfig;
import vn.tnteco.mqtt.data.TrackingContextEnum;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public abstract class BaseConsumerRunner {

    @Setter(onMethod_ = {@Autowired, @Qualifier("mqttTaskScheduler")})
    private ThreadPoolTaskScheduler scheduler;

    private final ConcurrentHashMap<String, ConsumerRunner> consumerRunners = new ConcurrentHashMap<>();

    protected abstract List<ConsumerRunnerConfig> getConsumerConfigs();

    @PostConstruct
    private void init() {
        scheduler.scheduleWithFixedDelay(this::execute, Duration.of(5, ChronoUnit.MINUTES));
    }

    private void execute() {
        String correlationId = "MQTT-CONSUMER-RUNNER-" + UUID.randomUUID().toString().replace("-", "").toLowerCase();
        ThreadContext.put(TrackingContextEnum.CORRELATION_ID.getKey(), correlationId);
        log.debug("Start execute consumer runner config");
        Set<String> activeConsumerIds = this.saveNewRunnerAndStart(this.getConsumerConfigs());
        log.debug("Consumer runner active ids: {}", activeConsumerIds);
        this.validateConsumers(activeConsumerIds);
        log.debug("End execute consumer runner config");
        log.info("Consumer runners: {}", consumerRunners.keys().toString());
        ThreadContext.clearAll();
    }

    private Set<String> saveNewRunnerAndStart(List<ConsumerRunnerConfig> consumerRunnerConfigs) {
        Set<String> consumerIdSet = new HashSet<>();
        consumerRunnerConfigs.forEach(config -> {
            String consumerId = String.format("%s___%s___%s", config.getShareName(), config.getTopicName(), config.getMqttQos().toString());
            consumerIdSet.add(consumerId);
            if (!consumerRunners.containsKey(consumerId)) {
                ConsumerRunner consumerRunner = new ConsumerRunner(consumerId, config);
                consumerRunners.put(consumerId, consumerRunner);
                consumerRunner.run();
            }
        });
        return consumerIdSet;
    }

    private void validateConsumers(Set<String> activeConsumerIds) {
        consumerRunners.forEach((consumerId, consumerRunner) -> {
            if (!activeConsumerIds.contains(consumerId)) { // Runner inactive
                if (consumerRunner.isStop()) { // Runner must stop to remove
                    consumerRunners.remove(consumerId);
                } else { // Runner hasn't stopped must stop
                    consumerRunner.stop();
                }
            } else { // Runner active
                if (consumerRunner.isStop()) {
                    consumerRunner.run();
                } else {
                    log.debug("Consumer: {} is running", consumerId);
                }
            }
        });
    }
}
