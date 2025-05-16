package vn.tnteco.common.core.exception;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ServiceException extends RuntimeException {

   private final String code;

   private final String message;

    public ServiceException(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
