package vn.tnteco.kafka.publisher;

import vn.tnteco.kafka.extension.ListenableFutureCallback;

import java.util.Map;

public interface IKafkaPublisher {

    <T> void pushAsync(String topic, T message);

    <T> void pushAsync(String topic, T message, ListenableFutureCallback<String> callback);

    <T> void pushAsync(String topic, T message, Map<String, String> headers);

    <T> void pushAsync(String topic, String key, T message, Map<String, String> headers);


    <T> boolean pushSync(String topic, T message);

    <T> boolean pushSync(String topic, T message, Map<String, String> headers);

    <T> boolean pushSync(String topic, String key, T message, Map<String, String> headers);

}
