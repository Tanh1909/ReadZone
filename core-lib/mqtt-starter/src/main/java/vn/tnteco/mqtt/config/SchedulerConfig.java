package vn.tnteco.mqtt.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@ConditionalOnProperty(
        value = {"messaging.mqtt.enable"},
        havingValue = "true"
)
public class SchedulerConfig {

    @Bean
    public ThreadPoolTaskScheduler mqttTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(50);
        scheduler.setThreadNamePrefix("si-mqtt-");
        scheduler.initialize();
        return scheduler;
    }

}