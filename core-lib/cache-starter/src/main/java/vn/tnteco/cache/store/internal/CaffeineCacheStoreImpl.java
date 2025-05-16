package vn.tnteco.cache.store.internal;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@Log4j2
@Primary
@RequiredArgsConstructor
@Service("CaffeineCacheStore")
@ConditionalOnProperty(
        value = {"internal-cache.enable"},
        havingValue = "true"
)
public class CaffeineCacheStoreImpl implements IRxInternalCacheStore, IInternalCacheStore {

    @Qualifier("caffeineCacheManager")
    private final CacheManager caffeineCacheManager;

    @Override
    public void putAllBlocking(String cacheName, Map<Object, Object> data) {
        Cache cache = caffeineCacheManager.getCache(cacheName);
        if (cache != null) data.forEach(cache::put);
    }

    @Override
    public void putBlocking(String cacheName, Object k, Object v) {
        Cache cache = caffeineCacheManager.getCache(cacheName);
        if (cache != null) cache.put(k, v);
    }

    @Override
    public <T> T getBlocking(String cacheName, Object key, Class<T> type) {
        Cache cache = caffeineCacheManager.getCache(cacheName);
        if (cache != null) return cache.get(key, type);
        return null;
    }

    @Override
    public <T, R> R getBlocking(String cacheName, Object key, Class<T> type, Function<? super T, ? extends R> handleCache) {
        Cache cache = caffeineCacheManager.getCache(cacheName);
        return handleCache.apply(cache != null ? cache.get(key, type) : null);
    }

    @Override
    public <T, R> R getAndPutBlocking(String cacheName, Object key, Class<T> type, Function<? super T, ? extends R> handleCache) {
        Cache cache = caffeineCacheManager.getCache(cacheName);
        T cacheValue = cache != null ? cache.get(key, type) : null;
        R value = handleCache.apply(cacheValue);
        if (value != null) this.putBlocking(cacheName, key, value);
        return value;
    }

    @Override
    public <T> Optional<T> getAndPutIfEmptyBlocking(String cacheName, Object key, Class<T> type, Supplier<Optional<T>> supplier) {
        Cache cache = caffeineCacheManager.getCache(cacheName);
        Optional<T> cacheValue = Optional.ofNullable(cache != null ? cache.get(key, type) : null);
        if (cacheValue.isPresent()) {
            log.info("get internalCache value: {}", cacheValue.get());
            return cacheValue;
        }
        Optional<T> newValue = supplier.get();
        newValue.ifPresent(value -> {
            log.info("not found in internalCache!Put in internalCache value: {}", value);
            putBlocking(cacheName, key, value);
        });
        return newValue;
    }

    @Override
    public void clearCacheBlocking(String cacheName) {
        Cache cache = caffeineCacheManager.getCache(cacheName);
        if (cache != null) cache.clear();
    }

    @Override
    public Completable putAll(String cacheName, Map<Object, Object> data) {
        return Completable.fromAction(() -> {
            Cache cache = caffeineCacheManager.getCache(cacheName);
            if (cache != null) data.forEach(cache::put);
        });
    }

    @Override
    public Completable put(String cacheName, Object k, Object v) {
        return Completable.fromAction(() -> {
            Cache cache = caffeineCacheManager.getCache(cacheName);
            if (cache != null) cache.put(k, v);
        });
    }

    @Override
    public <T> Single<Optional<T>> get(String cacheName, Object key, Class<T> type) {
        Cache cache = caffeineCacheManager.getCache(cacheName);
        return Single.just(Optional.ofNullable(cache != null ? cache.get(key, type) : null));
    }

    public <T, R> Single<Optional<R>> get(String cacheName, Object key, Class<T> type,
                                          Function<? super T, ? extends Optional<R>> handleCache) {
        Cache cache = caffeineCacheManager.getCache(cacheName);
        return Single.just(handleCache.apply(cache != null ? cache.get(key, type) : null));
    }

    @Override
    public <T> Single<Optional<T>> getAndPutIfEmpty(String cacheName, Object key, Class<T> type,
                                                    Supplier<Single<Optional<T>>> supplier) {
        Cache cache = caffeineCacheManager.getCache(cacheName);
        Optional<T> cacheValue = Optional.ofNullable(cache != null ? cache.get(key, type) : null);
        if (cacheValue.isPresent()) {
            return Single.just(cacheValue);
        }
        return supplier.get()
                .map(newValue -> {
                    newValue.ifPresent(val -> this.putBlocking(cacheName, key, val));
                    return newValue;
                });
    }

    @Override
    public Completable clearCache(String cacheName) {
        return Completable.fromAction(() -> {
            Cache cache = caffeineCacheManager.getCache(cacheName);
            if (cache != null) cache.clear();
        });
    }
}
