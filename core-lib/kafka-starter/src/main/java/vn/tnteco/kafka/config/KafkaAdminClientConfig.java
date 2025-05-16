package vn.tnteco.kafka.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import vn.tnteco.kafka.config.properties.KafkaAdminProperties;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
@RequiredArgsConstructor
@ConditionalOnProperty(
        value = {"messaging.kafka.admin.enable"},
        havingValue = "true"
)
public class KafkaAdminClientConfig {
    private final KafkaAdminProperties kafkaAdminProperties;

    @Bean
    @ConditionalOnMissingBean
    public AdminClient adminClient() {
        Map<String, Object> config = new HashMap<>();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaAdminProperties.getBootstrapServers());
        config.putAll(kafkaAdminProperties.buildProperties());
        return AdminClient.create(config);
    }
}
