package vn.tnteco.kafka.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import vn.tnteco.kafka.config.properties.KafkaListenerProperties;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(
        value = {"messaging.kafka.enable"},
        havingValue = "true"
)
public class KafkaListenerConfig {

    private final KafkaListenerProperties kafkaListenerProperties;

    @Bean
    @ConditionalOnMissingBean
    public ConcurrentKafkaListenerContainerFactory<String, Object>
    kafkaListenerContainerFactory(@Qualifier("defaultKafkaTemplate") KafkaTemplate<String, String> kafkaTemplate,
                                  ConsumerFactory<String, Object> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setReplyTemplate(kafkaTemplate);
        this.configureListenerFactory(factory);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object>
    kafkaBatchListenerContainerFactory(@Qualifier("defaultKafkaTemplate") KafkaTemplate<String, String> kafkaTemplate,
                                       ConsumerFactory<String, Object> batchConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(batchConsumerFactory);
        factory.setReplyTemplate(kafkaTemplate);
        factory.setBatchListener(true);
        this.configureListenerFactory(factory);
        return factory;
    }

    private void configureListenerFactory(ConcurrentKafkaListenerContainerFactory<String, Object> factory) {
        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
        ContainerProperties containerProperties = factory.getContainerProperties();
        map.from(kafkaListenerProperties::getConcurrency).to(factory::setConcurrency);
        map.from(kafkaListenerProperties::isAutoStartup).to(factory::setAutoStartup);
        map.from(kafkaListenerProperties::getAckMode).to(containerProperties::setAckMode);
        map.from(kafkaListenerProperties::getAsyncAcks).to(containerProperties::setAsyncAcks);
        map.from(kafkaListenerProperties::getClientId).to(containerProperties::setClientId);
        map.from(kafkaListenerProperties::getAckCount).to(containerProperties::setAckCount);
        map.from(kafkaListenerProperties::getAckTime).as(Duration::toMillis).to(containerProperties::setAckTime);
        map.from(kafkaListenerProperties::getPollTimeout).as(Duration::toMillis).to(containerProperties::setPollTimeout);
        map.from(kafkaListenerProperties::getNoPollThreshold).to(containerProperties::setNoPollThreshold);
        map.from(kafkaListenerProperties::getIdleBetweenPolls).as(Duration::toMillis).to(containerProperties::setIdleBetweenPolls);
        map.from(kafkaListenerProperties::getIdleEventInterval).as(Duration::toMillis).to(containerProperties::setIdleEventInterval);
        map.from(kafkaListenerProperties::getIdlePartitionEventInterval).as(Duration::toMillis).to(containerProperties::setIdlePartitionEventInterval);
        map.from(kafkaListenerProperties::getMonitorInterval).as(Duration::getSeconds).as(Number::intValue).to(containerProperties::setMonitorInterval);
        map.from(kafkaListenerProperties::getLogContainerConfig).to(containerProperties::setLogContainerConfig);
        map.from(kafkaListenerProperties::isMissingTopicsFatal).to(containerProperties::setMissingTopicsFatal);
        map.from(kafkaListenerProperties::isImmediateStop).to(containerProperties::setStopImmediate);
    }

}
