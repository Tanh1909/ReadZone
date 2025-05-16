package com.example.app.data.constant;

import org.springframework.http.HttpStatus;
import vn.tnteco.common.core.model.ResponseStatusCode;
import vn.tnteco.common.data.constant.ErrorResponseBase;

public interface AppErrorResponse extends ErrorResponseBase {

    ResponseStatusCode USERNAME_NOT_FOUND = ResponseStatusCode.builder().code("USERNAME NOT FOUND").httpStatus(HttpStatus.BAD_REQUEST).build();
    ResponseStatusCode PASSWORD_INVALID = ResponseStatusCode.builder().code("PASSWORD INVALID").httpStatus(HttpStatus.BAD_REQUEST).build();
    ResponseStatusCode EMAIL_HAS_EXIST = ResponseStatusCode.builder().code("EMAIL HAS EXIST").httpStatus(HttpStatus.BAD_REQUEST).build();
    ResponseStatusCode BOOK_NOT_FOUND = ResponseStatusCode.builder().code("BOOK NOT FOUND!").httpStatus(HttpStatus.BAD_REQUEST).build();
    ResponseStatusCode ORDER_IS_EMPTY = ResponseStatusCode.builder().code("ORDER IS EMPTY!").httpStatus(HttpStatus.BAD_REQUEST).build();
    ResponseStatusCode BOOK_IS_OVER_STOCK = ResponseStatusCode.builder().code("BOOK IS OVER STOCK!").httpStatus(HttpStatus.BAD_REQUEST).build();
    ResponseStatusCode PAYMENT_METHOD_NOT_SUPPORTED = ResponseStatusCode.builder().code("PAYMENT METHOD NOT SUPPORTED!").httpStatus(HttpStatus.BAD_REQUEST).build();
    ResponseStatusCode PAYMENT_REQUEST_IS_PROCESSING = ResponseStatusCode.builder().code("PAYMENT REQUEST IS PROCESSING").httpStatus(HttpStatus.BAD_REQUEST).build();
    ResponseStatusCode ORDER_HAS_BEEN_PAID = ResponseStatusCode.builder().code("ORDER HAS BEEN PAID!").httpStatus(HttpStatus.BAD_REQUEST).build();
    ResponseStatusCode ORDER_IS_PROCESSING_PAYMENT = ResponseStatusCode.builder().code("ORDER IS PROCESSING PAYMENT!").httpStatus(HttpStatus.BAD_REQUEST).build();
    ResponseStatusCode ORDER_NOT_FOUND = ResponseStatusCode.builder().code("ORDER NOT FOUND!").httpStatus(HttpStatus.BAD_REQUEST).build();
    ResponseStatusCode VNPAY_SECURE_HASH_IS_INVALID = ResponseStatusCode.builder().code("VNPAY SECURE HASH IS INVALID!").httpStatus(HttpStatus.BAD_REQUEST).build();
    ResponseStatusCode PAYMENT_FAILED = ResponseStatusCode.builder().code("PAYMENT FAILED!").httpStatus(HttpStatus.BAD_REQUEST).build();
    ResponseStatusCode PAYMENT_NOT_FOUND = ResponseStatusCode.builder().code("PAYMENT FAILED!").httpStatus(HttpStatus.BAD_REQUEST).build();
    ResponseStatusCode AUTHOR_NOT_FOUND = ResponseStatusCode.builder().code("AUTHOR NOT FOUND!").httpStatus(HttpStatus.BAD_REQUEST).build();
    ResponseStatusCode UPLOAD_FILE_FAIL = ResponseStatusCode.builder().code("UPLOAD FILE FAIL!").httpStatus(HttpStatus.BAD_REQUEST).build();


}
