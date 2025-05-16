package vn.tnteco.kafka.extension;

@FunctionalInterface
public interface FailureCallback {
    void onFailure(Throwable ex);
}
