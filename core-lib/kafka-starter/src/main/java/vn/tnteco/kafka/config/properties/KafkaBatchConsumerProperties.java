package vn.tnteco.kafka.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "messaging.kafka.batch")
public class KafkaBatchConsumerProperties extends KafkaConsumerProperties {
}
