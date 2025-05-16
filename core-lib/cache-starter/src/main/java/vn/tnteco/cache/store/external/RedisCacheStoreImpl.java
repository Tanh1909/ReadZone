package vn.tnteco.cache.store.external;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Service;
import vn.tnteco.cache.config.properties.ExternalCacheConfigProperties;
import vn.tnteco.cache.config.serializer.RedisSerializer;
import vn.tnteco.cache.data.functional.Callback;
import vn.tnteco.common.core.exception.ApiException;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Log4j2
@Primary
@RequiredArgsConstructor
@Service("redisCacheStore")
@ConditionalOnProperty(
        value = {"external-cache.enable"},
        havingValue = "true"
)
public class RedisCacheStoreImpl implements IExternalCacheStore {

    private final RedissonClient redissonClient;

    private final StringRedisTemplate stringRedisTemplate;

    private final RedisSerializer redisSerializer;

    private final ExternalCacheConfigProperties cacheConfigProp;

    private String keyGen(String key) {
        if (cacheConfigProp.getIsGenKey()) {
            return this.cacheConfigProp.getApplicationCache() + this.cacheConfigProp.getDelimiter() + key;
        }
        return key;
    }

    @Override
    public <T> void putObject(String key, T value) {
        this.putObject(key, value, cacheConfigProp.getCacheDefaultExpire());
    }

    @Override
    public <T> void putObject(String key, T value, long expireSeconds) {
        String keyGen = this.keyGen(key);
        log.debug("RedisCache put: key = {}, value = {}, expire = {}", keyGen, value != null ? value.toString() : null, expireSeconds);
        ValueOperations<String, String> ops = this.stringRedisTemplate.opsForValue();
        ops.set(keyGen, redisSerializer.serializer(value), Duration.ofSeconds(expireSeconds));
    }

    @Override
    public <T> T getObject(String key, Class<T> objectClass) {
        String keyGen = this.keyGen(key);
        log.debug("RedisCache get: key = {}", keyGen);
        String valueStr = this.stringRedisTemplate.opsForValue().get(keyGen);
        return redisSerializer.deserializer(valueStr, objectClass);
    }


    @Override
    public void redLock(String key, long lockSeconds, long expireSeconds, Callback callback) {
        String keyGen = this.keyGen(key);
        RLock lock = redissonClient.getLock("LOCK::" + keyGen);
        try {
            boolean isLock = lock.tryLock(lockSeconds, expireSeconds, TimeUnit.SECONDS);
            if (!isLock) {
                throw new RuntimeException("can't get lock for: " + keyGen);
            }
            callback.call();
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("error when try lock key " + keyGen, e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public Long increment(String key, long delta) {
        return this.increment(key, 0, delta, cacheConfigProp.getCacheDefaultExpire());
    }

    @Override
    public Long increment(String key, long delta, long expireSeconds) {
        return this.increment(key, 0, delta, expireSeconds);
    }

    @Override
    public Long decrement(String key, long delta, long expireSeconds) {
        return this.increment(key, 0, delta, expireSeconds);
    }

    @Override
    public Long decrement(String key, long delta) {
        return this.decrement(key, 0, delta, this.cacheConfigProp.getCacheDefaultExpire());
    }

    @Override
    public Long increment(String key, long init, long delta, long expireSeconds) {
        String keyGen = this.keyGen(key);
        byte[] rawKey = redisSerializer.serializerRaw(keyGen);
        Expiration expiration = Expiration.from(expireSeconds, TimeUnit.SECONDS);
        log.debug("RedisCache increment: key = {}", keyGen);
        return this.stringRedisTemplate.execute((RedisCallback<Long>) connection -> {
            try (connection) {
                byte[] rawValue = redisSerializer.serializerRaw(String.valueOf(init));
                connection.set(rawKey, rawValue, expiration, RedisStringCommands.SetOption.ifAbsent());
                return connection.incrBy(rawKey, delta);
            }
        });
    }

    @Override
    public Long decrement(String key, long init, long delta, long expireSeconds) {
        String keyGen = this.keyGen(key);
        byte[] rawKey = redisSerializer.serializerRaw(keyGen);
        Expiration expiration = Expiration.from(expireSeconds, TimeUnit.SECONDS);
        log.debug("RedisCache decrement: key = {}", keyGen);
        return this.stringRedisTemplate.execute((RedisCallback<Long>) connection -> {
            try (connection) {
                byte[] rawValue = redisSerializer.serializerRaw(String.valueOf(init));
                connection.set(rawKey, rawValue, expiration, RedisStringCommands.SetOption.ifAbsent());
                return connection.decrBy(rawKey, delta);
            }
        });
    }

    @Override
    public <K extends Comparable<? super K>, V> void putObjectAsHash(String key, Map<K, V> value) {
        this.putObjectAsHash(key, value, this.cacheConfigProp.getCacheDefaultExpire());
    }

    @Override
    public <K extends Comparable<? super K>, V> void putObjectAsHash(String key, Map<K, V> value, boolean isGenKey) {
        this.putObjectAsHash(key, value, cacheConfigProp.getCacheDefaultExpire(), isGenKey);
    }

    @Override
    public <K extends Comparable<? super K>, V> void putObjectAsHash(String key, Map<K, V> value, long expire, boolean isGenKey) {
        String keyGen = isGenKey ? this.keyGen(key) : key;
        log.debug("RedisCache put: key = {}, value = {}", keyGen, value.toString());
        byte[] rawKey = redisSerializer.serializerRaw(keyGen);
        Map<byte[], byte[]> entries = LinkedHashMap.newLinkedHashMap(value.size());
        for (Map.Entry<K, V> entry : value.entrySet()) {
            byte[] hashKeyRaw = redisSerializer.serializerRaw(entry.getKey());
            byte[] hashValueRaw = redisSerializer.serializerRaw(entry.getValue());
            entries.put(hashKeyRaw, hashValueRaw);
        }
        this.stringRedisTemplate.execute((RedisCallback<Object>) connection -> {
            try (connection) {
                connection.hMSet(rawKey, entries);
                connection.expire(rawKey, expire);
            }
            return null;
        });
    }

    @Override
    public <K extends Comparable<? super K>, V> void putObjectAsHash(String key, Map<K, V> value, long expire) {
        this.putObjectAsHash(key, value, expire, cacheConfigProp.getIsGenKey());
    }

    @Override
    public <K extends Comparable<? super K>, V> void putObjectToHash(String key, K hashKey, V hashValue) {
        this.putObjectToHash(key, hashKey, hashValue, cacheConfigProp.getIsGenKey());
    }

    @Override
    public <K extends Comparable<? super K>, V> void putObjectToHash(String key, K hashKey, V hashValue, boolean isGenKey) {
        String keyGen = isGenKey ? this.keyGen(key) : key;
        log.debug("RedisCache put: key = {}, hashKey = {}, hashValue = {}", keyGen, hashKey != null ? hashKey.toString() : null, hashValue);
        this.stringRedisTemplate.opsForHash()
                .put(keyGen, redisSerializer.serializer(hashKey), redisSerializer.serializer(hashValue));

    }

    @Override
    public <K extends Comparable<? super K>, V> Map<K, V> getObjectAsHash(String key, Class<K> objectClassKey, Class<V> objectClassValue) {
        return this.getObjectAsHash(key, objectClassKey, objectClassValue, cacheConfigProp.getIsGenKey());
    }

    @Override
    public <K extends Comparable<? super K>, V> Map<K, V> getObjectAsHash(String key, Class<K> objectClassKey, Class<V> objectClassValue, boolean isGenKey) {
        String keyGen = isGenKey ? this.keyGen(key) : key;
        log.debug("RedisCacheTemplate get: key = {}", keyGen);
        Map<byte[], byte[]> entries = this.stringRedisTemplate.execute((RedisCallback<Map<byte[], byte[]>>) connection -> {
            return connection.hGetAll(redisSerializer.serializerRaw(keyGen));
        });
        if (entries == null || entries.isEmpty()) {
            log.debug("Key {} does not exist", keyGen);
            return Collections.emptyMap();
        }
        Map<K, V> hashes = LinkedHashMap.newLinkedHashMap(entries.size());
        for (Map.Entry<byte[], byte[]> entry : entries.entrySet()) {
            K deserializeHashKey = redisSerializer.deserializerRaw(entry.getKey(), objectClassKey);
            V deserializeHashValue = redisSerializer.deserializerRaw(entry.getValue(), objectClassValue);
            hashes.put(deserializeHashKey, deserializeHashValue);
        }
        return hashes;
    }

    @Override
    public <K extends Comparable<? super K>, V> V getObjectFromHash(String key, K hashKey, Class<V> objectClassValue) {
        return this.getObjectFromHash(key, hashKey, objectClassValue, cacheConfigProp.getIsGenKey());
    }

    @Override
    public <K extends Comparable<? super K>, V> V getObjectFromHash(String key, K hashKey, Class<V> objectClassValue, boolean isGenKey) {
        String keyGen = isGenKey ? this.keyGen(key) : key;
        log.debug("RedisCache get: key = {}, hashKey = {}", keyGen, hashKey);
        String hashValueStr = (String) this.stringRedisTemplate.opsForHash().get(keyGen, redisSerializer.serializer(hashKey));
        return redisSerializer.deserializer(hashValueStr, objectClassValue);
    }

    @Override
    @SafeVarargs
    public final <K extends Comparable<? super K>> void deleteHashValue(String key, K... hashKeys) {
        String keyGen = this.keyGen(key);
        log.debug("RedisCache del: key = {}, hashKeys = {}", keyGen, hashKeys);
        Object[] hashKeysStr = Arrays.stream(hashKeys).map(redisSerializer::serializer).toArray(Object[]::new);
        this.stringRedisTemplate.opsForHash().delete(keyGen, hashKeysStr);
    }

    @Override
    public <K extends Comparable<? super K>> Long incrementHashValue(String key, K hashKey, long delta) {
        String keyGen = this.keyGen(key);
        log.debug("RedisCache increment: key = {}, hashKeys = {}", keyGen, hashKey);
        return this.stringRedisTemplate.opsForHash().increment(keyGen, redisSerializer.serializer(hashKey), delta);
    }

    @Override
    public <K extends Comparable<? super K>> Long decrementHashValue(String key, K hashKey, long delta) {
        String keyGen = this.keyGen(key);
        log.debug("RedisCache decrement: key = {}, hashKeys = {}", keyGen, hashKey);
        return this.stringRedisTemplate.opsForHash().increment(keyGen, redisSerializer.serializer(hashKey), -delta);
    }

    @Override
    public void delete(String key) {
        String keyGen = this.keyGen(key);
        log.debug("RedisCache delete: key = {}", keyGen);
        stringRedisTemplate.delete(keyGen);
    }

    @Override
    public boolean hasKey(String key) {
        return hasKey(key, cacheConfigProp.getIsGenKey());
    }

    @Override
    public boolean hasKey(String key, boolean isGenKey) {
        try {
            String keyGen = isGenKey ? this.keyGen(key) : key;
            return Boolean.TRUE.equals(this.stringRedisTemplate.hasKey(keyGen));
        } catch (Exception e) {
            return false;
        }
    }
}
