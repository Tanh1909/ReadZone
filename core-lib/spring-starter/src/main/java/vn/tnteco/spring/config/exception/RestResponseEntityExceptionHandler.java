package vn.tnteco.spring.config.exception;

import jakarta.validation.UnexpectedTypeException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import vn.tnteco.common.config.locale.Translator;
import vn.tnteco.common.core.exception.ApiException;
import vn.tnteco.common.core.exception.BusinessException;
import vn.tnteco.common.core.exception.DBException;
import vn.tnteco.common.core.exception.ServiceException;
import vn.tnteco.common.core.model.ResponseStatusCode;
import vn.tnteco.common.data.constant.ErrorResponseBase;
import vn.tnteco.spring.model.DfResponse;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;

@Log4j2
@RestControllerAdvice
public class RestResponseEntityExceptionHandler {

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors()
                .forEach(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    errors.put(fieldName, errorMessage);
                });
        return errors;
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Object> handleException(MissingServletRequestParameterException ex, WebRequest request) {
        String name = ex.getParameterName();
        return generateExceptionResponse(BAD_REQUEST.value(), "Missing " + name);
    }

    @ExceptionHandler(UnexpectedTypeException.class)
    public ResponseEntity<Object> handleUnexpectedTypeException(UnexpectedTypeException ex, WebRequest request) {
        log.error("UnexpectedTypeException ", ex);
        return generateExceptionResponse(INTERNAL_SERVER_ERROR.value(), INTERNAL_SERVER_ERROR.getReasonPhrase());
    }

    @ExceptionHandler(UndeclaredThrowableException.class)
    public ResponseEntity<Object> handleThrowableException(UndeclaredThrowableException ex, WebRequest request) {
        log.error("UndeclaredThrowableException ", ex);
        return generateExceptionResponse(INTERNAL_SERVER_ERROR.value(), INTERNAL_SERVER_ERROR.getReasonPhrase());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleNotReadable(HttpMessageNotReadableException ex, WebRequest request) {
        return generateExceptionResponse(NOT_ACCEPTABLE.value(), ex.getLocalizedMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Object> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex, WebRequest request) {
        return generateExceptionResponse(METHOD_NOT_ALLOWED.value(), ex.getLocalizedMessage());
    }

    // === Custom exception here === //
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex, WebRequest request) {
        log.error("Rest Exception ", ex);
        return generateExceptionResponse(ErrorResponseBase.INTERNAL_GENERAL_SERVER_ERROR);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Object> handleApiException(ApiException ex, WebRequest request) {
        log.error("ApiException ", ex);
        return generateExceptionResponse(ex.getResponseStatusCode(), ex.getParams());
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<Object> handleServiceException(ServiceException ex, WebRequest request) {
        log.error("ServiceException ", ex);
        return generateExceptionResponse(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(DBException.class)
    public ResponseEntity<Object> handleDBException(DBException ex, WebRequest request) {
        log.error("DBException ", ex);
        return generateExceptionResponse(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Object> handleBusinessException(BusinessException ex, WebRequest request) {
        log.error("BusinessException ", ex);
        return generateExceptionResponse(ErrorResponseBase.BUSINESS_ERROR);
    }

    private ResponseEntity<Object> generateExceptionResponse(Integer statusCode, String message) {
        if (statusCode == INTERNAL_SERVER_ERROR.value()) log.error(message);
        HttpStatus resolve = resolve(statusCode);
        if (resolve == null) resolve = BAD_REQUEST;
        return ResponseEntity.status(resolve)
                .body(Map.of(
                        "code", statusCode.toString(),
                        "message", message
                ));
    }

    private ResponseEntity<Object> generateExceptionResponse(String code, String message) {
        return ResponseEntity.status(BAD_REQUEST)
                .body(Map.of(
                    "code", code,
                    "message", message
                ));
    }

    private ResponseEntity<Object> generateExceptionResponse(ResponseStatusCode responseStatusCode) {
        DfResponse<Object> response = new DfResponse<>();
        response.setCode(responseStatusCode.getCode());
        response.setMessage(Translator.toLocale(responseStatusCode.getCode()));
        return ResponseEntity.status(responseStatusCode.getHttpStatus()).body(response);
    }

    private ResponseEntity<Object> generateExceptionResponse(ResponseStatusCode responseStatusCode, String[] params) {
        DfResponse<Object> response = new DfResponse<>();
        response.setCode(responseStatusCode.getCode());
        response.setMessage(Translator.toLocale(responseStatusCode.getCode(), params));
        return ResponseEntity.status(responseStatusCode.getHttpStatus()).body(response);
    }
}
