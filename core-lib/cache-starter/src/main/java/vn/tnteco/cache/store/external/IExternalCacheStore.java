package vn.tnteco.cache.store.external;

import vn.tnteco.cache.data.functional.Callback;

import java.util.Map;

public interface IExternalCacheStore {

    <T> void putObject(String key, T value);

    <T> void putObject(String key, T value, long expireSeconds);

    <T> T getObject(String key, Class<T> objectClass);

    void redLock(String key, long lockSeconds, long expireSeconds, Callback callback);

    Long increment(String key, long delta);

    Long increment(String key, long delta, long expireSeconds);

    Long decrement(String key, long delta, long expireSeconds);

    Long decrement(String key, long delta);

    Long increment(String key, long init, long delta, long expireSeconds);

    Long decrement(String key, long init, long delta, long expireSeconds);

    <K extends Comparable<? super K>, V> void putObjectAsHash(String key, Map<K, V> value);

    <K extends Comparable<? super K>, V> void putObjectAsHash(String key, Map<K, V> value, boolean isGenKey);

    <K extends Comparable<? super K>, V> void putObjectAsHash(String key, Map<K, V> value, long expireSeconds, boolean isGenKey);

    <K extends Comparable<? super K>, V> void putObjectAsHash(String key, Map<K, V> value, long expireSeconds);

    <K extends Comparable<? super K>, V> void putObjectToHash(String key, K hashKey, V hashValue);

    <K extends Comparable<? super K>, V> void putObjectToHash(String key, K hashKey, V hashValue, boolean isGenKey);


    <K extends Comparable<? super K>, V> Map<K, V> getObjectAsHash(String key, Class<K> objectClassKey, Class<V> objectClassValue);

    <K extends Comparable<? super K>, V> Map<K, V> getObjectAsHash(String key, Class<K> objectClassKey, Class<V> objectClassValue, boolean isGenKey);


    <K extends Comparable<? super K>, V> V getObjectFromHash(String key, K hashKey, Class<V> objectClassValue);

    <K extends Comparable<? super K>, V> V getObjectFromHash(String key, K hashKey, Class<V> objectClassValue, boolean isGenKey);


    <K extends Comparable<? super K>> void deleteHashValue(String key, K... hashKeys);

    <K extends Comparable<? super K>> Long incrementHashValue(String key, K hashKey, long delta);

    <K extends Comparable<? super K>> Long decrementHashValue(String key, K hashKey, long delta);

    void delete(String key);

    boolean hasKey(String key);

    boolean hasKey(String key, boolean isGenKey);


}
