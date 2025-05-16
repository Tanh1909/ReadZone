package com.example.app.vnpay.service;

import com.example.app.vnpay.data.request.VNPayParamsRequest;

import java.util.Map;

public interface IVNPayService {

    String createPaymentUrl(VNPayParamsRequest vNPayParamsRequest);

    boolean validateParamsCallBack(Map<String, String> callBackParams);

    Long getExpirationDate();
}
