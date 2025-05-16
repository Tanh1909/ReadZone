package vn.tnteco.kafka.extension;

import org.springframework.lang.Nullable;

@FunctionalInterface
public interface SuccessCallback<T> {
    void onSuccess(@Nullable T result);
}
