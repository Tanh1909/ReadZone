package vn.tnteco.kafka.runner;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.kafka.listener.MessageListener;

@Data
@Accessors(chain = true)
public class ConsumerRunnerConfig {
    private String groupId;
    private String topic;
    private MessageListener<String, String> messageListener;

}
