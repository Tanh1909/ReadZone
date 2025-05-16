package vn.tnteco.common.core.template;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.ThreadContext;
import vn.tnteco.common.data.constant.TrackingContextEnum;

@Log4j2
public class RxFlowableTemplate {

    @FunctionalInterface
    public interface EmitterSupplier<T> {
        void emit(FlowableEmitter<T> emitter) throws Exception;
    }

    public static <T> Flowable<T> create(EmitterSupplier<T> emitter) {
        String correlationId = ThreadContext.get(TrackingContextEnum.CORRELATION_ID.getKey());
        return Flowable.just("io")
                .subscribeOn(Schedulers.io())
                .flatMap(s -> create(emitter, correlationId));
    }

    private static <T> Flowable<T> create(EmitterSupplier<T> supplier, String correlationId) {
        return Flowable.create(emitter -> {
            ThreadContext.put(TrackingContextEnum.CORRELATION_ID.getKey(), correlationId);
            try {
                log.info("[THREAD] {}, supplier: {}", Thread.currentThread().getName(), supplier.getClass().getName());
                supplier.emit(emitter);
            } finally {
                ThreadContext.clearAll();
            }
        }, BackpressureStrategy.BUFFER);
    }

}
