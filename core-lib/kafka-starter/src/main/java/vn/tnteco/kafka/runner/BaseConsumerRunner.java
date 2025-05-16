package vn.tnteco.kafka.runner;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DeleteConsumerGroupsResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public abstract class BaseConsumerRunner {
    @Setter(onMethod_ = {@Autowired, @Qualifier("kafkaListenerContainerFactory")})
    private ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory;

    @Setter(onMethod_ = @Autowired)
    private AdminClient adminClient;
    private final ConcurrentHashMap<String, ConcurrentMessageListenerContainer<String, Object>> consumerGroups = new ConcurrentHashMap<>();

    public abstract List<ConsumerRunnerConfig> getConsumerRunnerConfigs();

    @Scheduled(fixedRate = 60000)
    public void syncConsumers() {
        List<ConsumerRunnerConfig> consumerRunnerConfigs = getConsumerRunnerConfigs();
        log.info("start sync consumers size: {}", consumerRunnerConfigs.size());
        consumerGroups.keySet().forEach(groupId -> {
            boolean exists = consumerRunnerConfigs.stream().anyMatch(config -> config.getGroupId().equals(groupId));
            if (!exists) {
                stopConsumer(groupId);
            }
        });
        consumerRunnerConfigs.forEach(config -> {
            if (!consumerGroups.containsKey(config.getGroupId())) {
                startConsumer(config);
            }
        });
    }

    public void startConsumer(ConsumerRunnerConfig config) {
        String groupId = config.getGroupId();
        log.info("Starting consumer for group {}", groupId);
        if (consumerGroups.containsKey(groupId)) return;
        ConcurrentMessageListenerContainer<String, Object> container = kafkaListenerContainerFactory.createContainer(config.getTopic());
        container.getContainerProperties()
                .setGroupId(groupId);
        container.setupMessageListener(config.getMessageListener());
        container.start();
        if (container.isRunning()) {
            log.info("Successfully start consumer group: {} ", groupId);
            consumerGroups.put(groupId, container);
            return;
        }
        log.error("Failed to start consumer group: {}", groupId);
    }

    public void stopConsumer(String groupId) {
        log.info("stopping consumer group {}", groupId);
        if (!consumerGroups.containsKey(groupId)) return;
        ConcurrentMessageListenerContainer<String, Object> container = consumerGroups.get(groupId);
        if (container == null) {
            return;
        }
        container.stop();
        if (!container.isRunning()) {
            log.info("Successfully stop consumer group: {} ", groupId);
            consumerGroups.remove(groupId);
            return;
        }
        log.error("Failed to stop consumer group: {} ", groupId);
    }

    public void deleteConsumer(String groupId) {
        log.info("delete consumer group {}", groupId);
        try {
            DeleteConsumerGroupsResult result = adminClient.deleteConsumerGroups(List.of(groupId));
            result.all().get();
        } catch (Exception e) {
            log.error("Failed to delete consumer group: {}", groupId, e);
        }


    }
}
