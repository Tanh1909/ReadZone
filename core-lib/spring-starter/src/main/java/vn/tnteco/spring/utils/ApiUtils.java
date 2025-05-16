package vn.tnteco.spring.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import vn.tnteco.common.core.exception.ApiException;
import vn.tnteco.common.core.exception.BusinessException;
import vn.tnteco.common.core.exception.ServiceException;
import vn.tnteco.common.core.extension.SupplierThrowable;
import vn.tnteco.common.core.json.Json;
import vn.tnteco.common.data.constant.ErrorResponseBase;
import vn.tnteco.spring.model.DfResponse;
import vn.tnteco.spring.model.HttpResponse;

@Log4j2
@UtilityClass
public class ApiUtils {

    public static <T> HttpResponse<DfResponse<T>> handleResponseInternal(SupplierThrowable<DfResponse<T>> supplierCallAPI) {
        try {
            return new HttpResponse<DfResponse<T>>()
                    .setHttpStatus(HttpStatus.OK)
                    .setResponse(supplierCallAPI.get());
        } catch (ResourceAccessException ex) {
            log.error("handleResponseInternal TIMEOUT", ex);
            throw new ApiException(ErrorResponseBase.INTERNAL_GENERAL_SERVER_ERROR);
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            TypeReference<DfResponse<T>> responseType = new TypeReference<>() {};
            return new HttpResponse<DfResponse<T>>()
                    .setHttpStatus(HttpStatus.valueOf(ex.getStatusCode().value()))
                    .setResponse(Json.decodeValue(ex.getResponseBodyAsString(), responseType));
        } catch (Exception e) {
            log.error("handleResponseInternal error", e);
            throw new BusinessException(e);
        }
    }

    public static <T> T handleResponseInternalReturnData(SupplierThrowable<DfResponse<T>> supplierCallAPI) {
        try {
            return supplierCallAPI.get().getData();
        } catch (ResourceAccessException ex) {
            log.error("handleResponseInternalReturnData TIMEOUT", ex);
            throw new ApiException(ErrorResponseBase.INTERNAL_GENERAL_SERVER_ERROR);
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            TypeReference<DfResponse<T>> responseType = new TypeReference<>() {};
            DfResponse<T> dfResponse = Json.decodeValue(ex.getResponseBodyAsString(), responseType);
            throw new ServiceException(dfResponse.getCode(), dfResponse.getMessage());
        } catch (Exception e) {
            log.error("handleResponseInternalReturnData error", e);
            throw new BusinessException(e);
        }
    }

}
