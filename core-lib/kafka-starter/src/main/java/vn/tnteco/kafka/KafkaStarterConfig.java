package vn.tnteco.kafka;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableAutoConfiguration(exclude = {
        KafkaAutoConfiguration.class
})
@ComponentScan({"vn.tnteco.kafka"})
@EnableScheduling
public class KafkaStarterConfig {
}
