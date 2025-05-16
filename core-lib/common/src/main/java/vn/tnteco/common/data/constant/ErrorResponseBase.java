package vn.tnteco.common.data.constant;

import org.springframework.http.HttpStatus;
import vn.tnteco.common.core.model.ResponseStatusCode;

@SuppressWarnings({"java:S1214"})
public interface ErrorResponseBase {

    ResponseStatusCode SUCCESS = ResponseStatusCode.builder().code("200").httpStatus(HttpStatus.OK).build();
    ResponseStatusCode BUSINESS_ERROR = ResponseStatusCode.builder().code("BUSINESS_ERROR").httpStatus(HttpStatus.BAD_REQUEST).build();
    ResponseStatusCode INTERNAL_GENERAL_SERVER_ERROR = ResponseStatusCode.builder().code("GENERAL_SERVER_ERROR").httpStatus(HttpStatus.INTERNAL_SERVER_ERROR).build();
    ResponseStatusCode RESOURCE_NOT_FOUND = ResponseStatusCode.builder().code("SI-COMMON-01").httpStatus(HttpStatus.BAD_REQUEST).build();
    ResponseStatusCode UNAUTHORIZED = ResponseStatusCode.builder().code("SI-COMMON-02").httpStatus(HttpStatus.UNAUTHORIZED).build();
    ResponseStatusCode NOT_PERMISSION = ResponseStatusCode.builder().code("SI-COMMON-03").httpStatus(HttpStatus.FORBIDDEN).build();
    ResponseStatusCode RECORD_NOT_FOUND = ResponseStatusCode.builder().code("SI-COMMON-04").httpStatus(HttpStatus.BAD_REQUEST).build();
    ResponseStatusCode EMPTY_DATA = ResponseStatusCode.builder().code("SI-COMMON-05").httpStatus(HttpStatus.BAD_REQUEST).build();
    ResponseStatusCode INPUT_WRONG = ResponseStatusCode.builder().code("SI-COMMON-06").httpStatus(HttpStatus.BAD_REQUEST).build();
    ResponseStatusCode CONFIG_NOT_FOUND = ResponseStatusCode.builder().code("SI-COMMON-07").httpStatus(HttpStatus.BAD_REQUEST).build();
    ResponseStatusCode CONFIG_INVALID = ResponseStatusCode.builder().code("SI-COMMON-08").httpStatus(HttpStatus.BAD_REQUEST).build();

}
