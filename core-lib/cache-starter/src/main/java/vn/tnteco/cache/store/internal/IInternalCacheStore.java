package vn.tnteco.cache.store.internal;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public interface IInternalCacheStore {

    void putAllBlocking(String cacheName, Map<Object, Object> data);

    void putBlocking(String cacheName, Object k, Object v);

    <T> T getBlocking(String cacheName, Object key, Class<T> type);

    <T, R> R getBlocking(String cacheName, Object key, Class<T> type,
                         Function<? super T, ? extends R> handleCache);

    <T, R> R getAndPutBlocking(String cacheName, Object key, Class<T> type,
                               Function<? super T, ? extends R> handleCache);

    <T> Optional<T> getAndPutIfEmptyBlocking(String cacheName, Object key, Class<T> type,
                                             Supplier<Optional<T>> supplier);

    void clearCacheBlocking(String cacheName);

}
