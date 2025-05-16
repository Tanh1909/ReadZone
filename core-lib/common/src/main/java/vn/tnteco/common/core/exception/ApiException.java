package vn.tnteco.common.core.exception;


import lombok.Getter;
import lombok.Setter;
import vn.tnteco.common.core.model.ResponseStatusCode;

@Setter
@Getter
public class ApiException extends RuntimeException {

    private final ResponseStatusCode responseStatusCode;
    private String[] params;
    private Object response;

    public ApiException(ResponseStatusCode responseStatusCode) {
        this.responseStatusCode = responseStatusCode;
    }

    public ApiException(ResponseStatusCode responseStatusCode, String[] params) {
        this.responseStatusCode = responseStatusCode;
        this.params = params;
    }

    public ApiException(ResponseStatusCode responseStatusCode, Object response) {
        this.responseStatusCode = responseStatusCode;
        this.response = response;
    }

    public ApiException(ResponseStatusCode responseStatusCode, Throwable cause) {
        super(cause);
        this.responseStatusCode = responseStatusCode;
    }

}
