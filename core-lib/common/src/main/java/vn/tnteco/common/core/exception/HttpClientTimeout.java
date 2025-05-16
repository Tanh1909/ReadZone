package vn.tnteco.common.core.exception;

public class HttpClientTimeout extends RuntimeException {

    public HttpClientTimeout(String message) {
        super(message);
    }

    public HttpClientTimeout(String message, Throwable cause) {
        super(message, cause);
    }

}
