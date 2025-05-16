package vn.tnteco.common.core.template;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.ThreadContext;
import vn.tnteco.common.core.context.SecurityContext;
import vn.tnteco.common.core.exception.ApiException;
import vn.tnteco.common.core.extension.SupplierThrowable;
import vn.tnteco.common.data.constant.ErrorResponseBase;
import vn.tnteco.common.data.constant.TrackingContextEnum;

import java.util.*;
import java.util.function.Function;

@Log4j2
public class RxTemplate {
    private RxTemplate() {
    }

    public static <T> Single<T> rxSchedulerIo(SupplierThrowable<T> supplier) {
        String correlationId = ThreadContext.get(TrackingContextEnum.CORRELATION_ID.getKey());
        Map<String, Object> contextManage = SecurityContext.getContextManage();
        return Single.just("io")
                .doOnSubscribe(d -> {
                    ThreadContext.clearAll();
                    SecurityContext.clearContext();
                })
                .doFinally(() -> log.info("[Release] [THREAD] {}, supplier: {}", Thread.currentThread().getName(), supplier.getClass().getName()))
                .subscribeOn(Schedulers.io())
                .flatMap(s -> rxBlockingAsync(supplier, contextManage, correlationId));
    }

    public static <T> Single<T> rxSchedulerNewThread(SupplierThrowable<T> supplier) {
        String correlationId = ThreadContext.get(TrackingContextEnum.CORRELATION_ID.getKey());
        Map<String, Object> contextManage = SecurityContext.getContextManage();
        return Single.just("new-thread")
                .doOnSubscribe(d -> {
                    ThreadContext.clearAll();
                    SecurityContext.clearContext();
                })
                .subscribeOn(Schedulers.newThread())
                .flatMap(s -> rxBlockingAsync(supplier, contextManage, correlationId));
    }

    public static <T> Single<T> rxSchedulerComputing(SupplierThrowable<T> supplier) {
        String correlationId = ThreadContext.get(TrackingContextEnum.CORRELATION_ID.getKey());
        Map<String, Object> contextManage = SecurityContext.getContextManage();
        return Single.just("computing")
                .doOnSubscribe(d -> {
                    ThreadContext.clearAll();
                    SecurityContext.clearContext();
                })
                .subscribeOn(Schedulers.computation())
                .flatMap(s -> rxBlockingAsync(supplier, contextManage, correlationId));
    }

    private static <T> Single<T> rxBlockingAsync(SupplierThrowable<T> supplier, Map<String, Object> contextManager, String correlationId) {
        return Single.create(emitter -> {
            SecurityContext.setContextManage(contextManager);
            ThreadContext.put(TrackingContextEnum.CORRELATION_ID.getKey(), correlationId);
            try {
                log.info("[THREAD] {}, supplier: {}", Thread.currentThread().getName(), supplier.getClass().getName());
                final T value = supplier.get();
                if (value != null) {
                    emitter.onSuccess(value);
                } else {
                    emitter.onError(new ApiException(ErrorResponseBase.EMPTY_DATA));
                }
            } catch (Exception exception) {
                log.error("[RX-BLOCKING-ASYNC] cause", exception);
                emitter.onError(exception);
            } finally {
                ThreadContext.clearAll();
                SecurityContext.clearContext();
            }
        });
    }

    public static <T> void rxSchedulerNewThreadSubscribe(SupplierThrowable<T> supplier) {
        String correlationId = ThreadContext.get(TrackingContextEnum.CORRELATION_ID.getKey());
        Map<String, Object> contextManage = SecurityContext.getContextManage();
        Single.just("new-thread")
                .doOnSubscribe(d -> {
                    ThreadContext.clearAll();
                    SecurityContext.clearContext();
                })
                .subscribeOn(Schedulers.newThread())
                .flatMap(s -> rxBlockingAsync(supplier, contextManage, correlationId))
                .subscribe(
                        result -> log.info("[rx-subscribe] execute job {}", result),
                        throwable -> log.error("[rx-subscribe] fail to execute job", throwable));
    }

    public static <T> void rxSubscribe(Single<T> single) {
        single.subscribe(
                result -> log.info("[rx-subscribe] execute job {}", result),
                throwable -> log.error("[rx-subscribe] fail to execute job", throwable));
    }

    public static <T> void rxSubscribe(Single<T> single, String message) {
        single.subscribe(
                result -> log.info("[rx-subscribe] {}", message),
                throwable -> log.error("[rx-subscribe] fail to execute job", throwable));
    }

    public static <T> void rxSubscribe(Single<T> single1, Single<T> single2) {
        Single.zip(single1, single2, (t1, t2) -> true)
                .subscribe(
                        result -> log.info("[rx-subscribe] execute job {}", result),
                        throwable -> log.error("[rx-subscribe] fail to execute job", throwable));
    }

    public static <T> Single<List<T>> zip(List<Single<T>> singles) {
        if (CollectionUtils.isEmpty(singles)) {
            return Single.just(Collections.emptyList());
        }
        return Single.zip(singles, result -> Arrays.stream(result)
                .filter(Objects::nonNull)
                .map(item -> (T) item).toList());
    }

    public static <T, R> Single<R> zip(List<Single<T>> singles, Function<List<T>, ? extends R> zipper) {
        if (CollectionUtils.isEmpty(singles)) {
            return Single.just(zipper.apply(Collections.emptyList()));
        }
        return Single.zip(singles, result -> {
            List<T> list = Arrays.stream(result)
                    .filter(Objects::nonNull)
                    .map(item -> (T) item).toList();
            return zipper.apply(list);
        });
    }

}
