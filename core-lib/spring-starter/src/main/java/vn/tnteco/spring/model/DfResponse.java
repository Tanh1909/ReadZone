package vn.tnteco.spring.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServletServerHttpResponse;
import vn.tnteco.common.config.jackson.JsonMapper;
import vn.tnteco.common.config.locale.Translator;
import vn.tnteco.common.core.exception.BusinessException;
import vn.tnteco.common.core.model.ResponseStatusCode;
import vn.tnteco.common.data.constant.ErrorResponseBase;

import java.io.IOException;

@Data
@Accessors(chain = true)
@Schema(description = "Generic response wrapper")
public class DfResponse<T> {

    private String code;

    private String message;

    private T data;

    public DfResponse() {
    }

    public DfResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public static <T> DfResponse<T> ok(T body) {
        String successCode = ErrorResponseBase.SUCCESS.getCode();
        DfResponse<T> response = new DfResponse<>();
        response.setData(body);
        response.setCode(successCode);
        response.setMessage(Translator.toLocale(successCode));
        return response;
    }

    public static <T> ResponseEntity<DfResponse<T>> okEntity(T body) {
        String successCode = ErrorResponseBase.SUCCESS.getCode();
        DfResponse<T> response = new DfResponse<>();
        response.setData(body);
        response.setCode(successCode);
        response.setMessage(Translator.toLocale(successCode));
        return ResponseEntity.ok(response);
    }

    public static void httpServletResponse(HttpServletResponse response, ResponseStatusCode responseStatusCode) {
        DfResponse<Object> dfResponse = new DfResponse<>();
        dfResponse.setCode(responseStatusCode.getCode());
        dfResponse.setMessage(Translator.toLocale(responseStatusCode.getCode()));
        try (ServletServerHttpResponse serverHttpResponse = new ServletServerHttpResponse(response)) {
            serverHttpResponse.setStatusCode(responseStatusCode.getHttpStatus());
            serverHttpResponse.getServletResponse().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            serverHttpResponse.getBody().write(JsonMapper.getObjectMapper().writeValueAsBytes(dfResponse));
        } catch (IOException e) {
            throw new BusinessException(e);
        }
    }

    public static void httpServletResponse(HttpServletResponse response, HttpStatus httpStatus, String code, String message) {
        DfResponse<Object> dfResponse = new DfResponse<>();
        dfResponse.setCode(code);
        dfResponse.setMessage(message);
        try (ServletServerHttpResponse serverHttpResponse = new ServletServerHttpResponse(response)) {
            serverHttpResponse.setStatusCode(httpStatus);
            serverHttpResponse.getServletResponse().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            serverHttpResponse.getBody().write(JsonMapper.getObjectMapper().writeValueAsBytes(dfResponse));
        } catch (IOException e) {
            throw new BusinessException(e);
        }
    }
}
