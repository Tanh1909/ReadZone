package vn.tnteco.cache.store.internal;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public interface IRxInternalCacheStore {

    Completable putAll(String cacheName, Map<Object, Object> data);

    Completable put(String cacheName, Object k, Object v);

    <T> Single<Optional<T>> get(String cacheName, Object key, Class<T> type);

    <T, R> Single<Optional<R>> get(String cacheName, Object key, Class<T> type,
                                   Function<? super T, ? extends Optional<R>> handleCache);

    <T> Single<Optional<T>> getAndPutIfEmpty(String cacheName, Object key, Class<T> type,
                                             Supplier<Single<Optional<T>>> supplierGetValueToCache);

    Completable clearCache(String cacheName);

}
