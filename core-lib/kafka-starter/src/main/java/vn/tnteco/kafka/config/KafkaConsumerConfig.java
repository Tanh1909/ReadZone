package vn.tnteco.kafka.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import vn.tnteco.kafka.config.properties.KafkaBatchConsumerProperties;
import vn.tnteco.kafka.config.properties.KafkaConsumerProperties;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
@RequiredArgsConstructor
@ConditionalOnProperty(
        value = {"messaging.kafka.enable"},
        havingValue = "true"
)
public class KafkaConsumerConfig {

    private final KafkaConsumerProperties kafkaConsumerProperties;
    private final KafkaBatchConsumerProperties kafkaBatchConsumerProperties;

    @Bean
    @ConditionalOnMissingBean({ConsumerFactory.class})
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> properties = new HashMap<>();
        // put default props
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "default");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, kafkaConsumerProperties.getMaxPollIntervalMs());
        // put config props
        properties.putAll(kafkaConsumerProperties.buildProperties());
        return new DefaultKafkaConsumerFactory<>(properties);
    }

    @Bean
    public ConsumerFactory<String, Object> batchConsumerFactory() {
        Map<String, Object> properties = new HashMap<>();
        // put default props
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "batch-consumer");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, kafkaBatchConsumerProperties.getMaxPollIntervalMs());
        // put config props
        properties.putAll(kafkaBatchConsumerProperties.buildProperties());
        return new DefaultKafkaConsumerFactory<>(properties);
    }

}
