package vn.tnteco.kafka.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import vn.tnteco.kafka.config.properties.KafkaProducerProperties;

import java.util.HashMap;
import java.util.Map;

@Getter
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(
        value = {"messaging.kafka.enable"},
        havingValue = "true"
)
public class KafkaProducerConfig {

    private final KafkaProducerProperties kafkaProducerProperties;

    @Bean("kafkaEventProducerFactory")
    @ConditionalOnMissingBean({ProducerFactory.class})
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> properties = new HashMap<>();
        // put default props
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, kafkaProducerProperties.getDeliveryTimeout());
        properties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, kafkaProducerProperties.getRequestTimeout());
        // put config props
        properties.putAll(kafkaProducerProperties.buildProperties());
        return new DefaultKafkaProducerFactory<>(properties);
    }

    @Bean("defaultKafkaTemplate")
    @ConditionalOnMissingBean({KafkaTemplate.class})
    public KafkaTemplate<String, String> defaultKafkaTemplate(ProducerFactory<String, String> kafkaProducerFactory) {
        return new KafkaTemplate<>(kafkaProducerFactory);
    }
}

