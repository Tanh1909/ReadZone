package com.example.app.service.kafka;

public interface IPushKafkaService {

    <T> boolean sendMessageSync(String topic, String key, T value);

}
