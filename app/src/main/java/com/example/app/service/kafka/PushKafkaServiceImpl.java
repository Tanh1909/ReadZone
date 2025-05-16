package com.example.app.service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;
import vn.tnteco.common.config.properties.ApplicationProperties;
import vn.tnteco.common.core.json.Json;
import vn.tnteco.common.utils.StringUtils;
import vn.tnteco.kafka.publisher.IKafkaPublisher;

import java.util.HashMap;
import java.util.Map;

@Log4j2
@Service
@RequiredArgsConstructor
public class PushKafkaServiceImpl implements IPushKafkaService {

    private final IKafkaPublisher kafkaPublisher;

    private final ApplicationProperties appProperties;

    @Override
    public <T> boolean sendMessageSync(String topic, String key, T value) {
        Map<String, String> headers = new HashMap<>();
        String newCorrelationId = StringUtils.genCorrelationId(appProperties.getApplicationShortName());
        headers.put(KafkaHeaders.CORRELATION_ID, newCorrelationId);
        log.debug("data before push kafka: {}", value);
        log.info("// Push Async Kafka: correlationId={}, topic={}, request={}", newCorrelationId, topic, Json.parserJsonLog(value));
        return kafkaPublisher.pushSync(topic, key, value, headers);
    }

}
