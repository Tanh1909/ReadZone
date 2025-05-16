package vn.tnteco.common.core.extension;

@FunctionalInterface
public interface SupplierThrowable<T> {
    T get() throws Exception;

}
