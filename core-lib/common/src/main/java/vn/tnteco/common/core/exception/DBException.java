package vn.tnteco.common.core.exception;

import lombok.Getter;
import lombok.Setter;
import vn.tnteco.common.core.model.ResponseStatusCode;

@Getter
@Setter
public class DBException extends RuntimeException {

    private final ResponseStatusCode responseStatusCode;
    private final String[] params;
    private int code = 404;

    public DBException(String message) {
        super(message);
        this.responseStatusCode = null;
        this.params = null;
    }

    public DBException(String message, Throwable cause) {
        super(message, cause);
        this.responseStatusCode = null;
        this.params = null;
    }

    public DBException(ResponseStatusCode responseStatusCode) {
        this.responseStatusCode = responseStatusCode;
        this.params = null;
    }

    public DBException(ResponseStatusCode responseStatusCode, Throwable cause) {
        super(cause);
        this.responseStatusCode = responseStatusCode;
        this.params = null;
    }

    public DBException(ResponseStatusCode responseStatusCode, String[] params) {
        this.responseStatusCode = responseStatusCode;
        this.params = params;
    }
}
